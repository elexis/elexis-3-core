package ch.elexis.core.data.service.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.common.InstanceStatus;
import ch.elexis.core.common.InstanceStatus.STATE;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.data.service.StoreToStringServiceHolder;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockRequest;
import ch.elexis.core.lock.types.LockRequest.Type;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IElexisServerService;
import ch.elexis.core.services.ILocalLockService;
import ch.elexis.core.services.IStoreToStringService;
import ch.elexis.data.PersistentObject;

/**
 * ILocalLockService implementation. Managing locks of PersistentObjects, or
 * Identifiable.</br>
 * If the environment variable <b>ELEXIS_SERVER_REST_INTERFACE_URL</b> is set a
 * connection to a remote LockService will used internal.
 *
 * @author marco
 *
 */
@Component
public class LocalLockService implements ILocalLockService {

	@Reference
	private IElexisServerService elexisServerService;

	@Reference
	private IContextService contextService;

	@Reference
	private IConfigService configService;

	@Reference
	private IStoreToStringService storeToStringService;

	private final HashMap<String, Integer> lockCount = new HashMap<String, Integer>();
	private final HashMap<String, LockInfo> locks = new HashMap<String, LockInfo>();
	private Logger logger = LoggerFactory.getLogger(LocalLockService.class);

	private Timer timer;

	@Activate
	public void activate() {
		timer = new Timer();
		timer.schedule(new LockRefreshTask(), 10000, 10000);
	}

	@Override
	public LockResponse releaseAllLocks() {
		if (elexisServerService.isStandalone()) {
			return LockResponse.OK;
		}

		List<LockInfo> lockList = new ArrayList<LockInfo>(locks.values());
		for (LockInfo lockInfo : lockList) {
			LockRequest lockRequest = new LockRequest(LockRequest.Type.RELEASE, lockInfo);
			LockResponse lr = acquireOrReleaseLocks(lockRequest);
			if (!lr.isOk()) {
				return lr;
			}
		}
		return LockResponse.OK;
	}

	@Override
	public LockResponse releaseLock(Object object) {
		if (object == null) {
			return LockResponse.DENIED(null);
		}
		logger.debug("Releasing lock on [" + object + "]");
		return releaseLock(StoreToStringServiceHolder.getStoreToString(object));
	}

	@Override
	public LockResponse releaseLock(LockInfo lockInfo) {
		if (lockInfo.getElementStoreToString() == null) {
			return LockResponse.DENIED(null);
		}
		logger.debug("Releasing lock on [" + lockInfo.getElementStoreToString() + "]");
		return releaseLock(lockInfo.getElementStoreToString());
	}

	@Override
	public LockResponse releaseLock(String storeToString) {
		IUser user = ContextServiceHolder.get().getActiveUser().orElse(null);
		LockInfo lil = new LockInfo(storeToString, user.getId(), elexisServerService.getSystemUuid().toString(),
				contextService.getStationIdentifier(),
				configService.getLocal(Preferences.STATION_IDENT_TEXT, StringUtils.EMPTY));
		LockRequest lockRequest = new LockRequest(LockRequest.Type.RELEASE, lil);
		return acquireOrReleaseLocks(lockRequest);
	}

	private String getId(Object object) {
		if (object instanceof PersistentObject) {
			return ((PersistentObject) object).getId();
		} else if (object instanceof Identifiable) {
			return ((Identifiable) object).getId();
		}
		throw new IllegalStateException("No id for [" + object + "]");
	}

	@Override
	public LockResponse acquireLockBlocking(Object object, int secTimeout, IProgressMonitor monitor) {
		if (object == null) {
			return LockResponse.DENIED(null);
		}
		if (monitor != null) {
			monitor.beginTask("Acquiring Lock ...", (secTimeout * 10) + 1);
		}
		logger.debug("Acquiring lock blocking on [" + object + "]");
		final String storeToString = StoreToStringServiceHolder.getStoreToString(object);

		LockResponse response = acquireLock(storeToString);
		int sleptMilli = 0;
		while (!response.isOk()) {
			if (response.getStatus() == LockResponse.Status.DENIED_PERMANENT) {
				return response;
			}

			try {
				Thread.sleep(100);
				sleptMilli += 100;
				response = acquireLock(storeToString);
				if (sleptMilli > (secTimeout * 1000)) {
					return response;
				}
				// update monitor
				if (monitor != null) {
					monitor.worked(1);
					if (monitor.isCanceled()) {
						return LockResponse.DENIED(response.getLockInfo());
					}
				}
			} catch (InterruptedException e) {
				// ignore and keep trying
			}
		}
		return response;
	}

	@Override
	public LockResponse acquireLock(Object object) {
		if (object == null) {
			return LockResponse.DENIED(null);
		}
		logger.debug("Acquiring lock on [" + object + "]");
		LockResponse lr = acquireLock(StoreToStringServiceHolder.getStoreToString(object));

		if (lr.getStatus() == LockResponse.Status.ERROR) {
			logger.warn("LockResponse ERROR");
		}

		return lr;
	}

	private LockResponse acquireLock(String storeToString) {
		if (storeToString == null) {
			return LockResponse.DENIED(null);
		}

		IUser user = ContextServiceHolder.get().getActiveUser().orElse(null);
		LockInfo lockInfo = new LockInfo(storeToString, user.getId(), elexisServerService.getSystemUuid().toString(),
				contextService.getStationIdentifier(),
				configService.getLocal(Preferences.STATION_IDENT_TEXT, StringUtils.EMPTY));
		LockRequest lockRequest = new LockRequest(LockRequest.Type.ACQUIRE, lockInfo);
		return acquireOrReleaseLocks(lockRequest);
	}

	@Override
	public LockResponse acquireOrReleaseLocks(LockRequest lockRequest) {
		if (elexisServerService.isStandalone()) {
			return LockResponse.OK(lockRequest.getLockInfo());
		}

		if (elexisServerService == null) {
			String message = "System not configured for standalone mode, and elexis-server not available!";
			logger.error(message);
			ElexisEventDispatcher.fireElexisStatusEvent(new ElexisStatus(org.eclipse.core.runtime.Status.ERROR,
					CoreHub.PLUGIN_ID, ElexisStatus.CODE_NONE, message, null));
			return new LockResponse(LockResponse.Status.ERROR, lockRequest.getLockInfo());
		}

		LockInfo lockInfo = lockRequest.getLockInfo();

		synchronized (locks) {
			// does the requested lock match the cache on our side?
			if (LockRequest.Type.ACQUIRE == lockRequest.getRequestType()
					&& locks.keySet().contains(lockInfo.getElementId())) {
				incrementLockCount(lockInfo);
				return LockResponse.OK(lockRequest.getLockInfo());
			}

			// do not release lock if it was locked multiple times
			if (LockRequest.Type.RELEASE == lockRequest.getRequestType() && getCurrentLockCount(lockInfo) > 1) {
				decrementLockCount(lockInfo);
				return LockResponse.OK(lockRequest.getLockInfo());
			}
			// TODO should we release all locks on acquiring a new one?
			// if yes, this has to be dependent upon the strategy
			try {
				if (LockRequest.Type.RELEASE == lockRequest.getRequestType()) {
					PersistentObject po = CoreHub.poFactory.createFromString(lockInfo.getElementStoreToString());
					if (po != null) {
						ElexisEventDispatcher.getInstance().fire(new ElexisEvent(po, po.getClass(),
								ElexisEvent.EVENT_LOCK_PRERELEASE, ElexisEvent.PRIORITY_SYNC));
					} else {
						Optional<Identifiable> identifiable = storeToStringService
								.loadFromString(lockInfo.getElementStoreToString());
						if (identifiable.isPresent()) {
							postEvent(ElexisEventTopics.EVENT_LOCK_PRERELEASE, identifiable.get(), true);
						}
					}
				}

				LockResponse lr = elexisServerService.acquireOrReleaseLocks(lockRequest);
				if (!lr.isOk()) {
					return lr;
				}

				if (LockRequest.Type.ACQUIRE == lockRequest.getRequestType()) {
					// ACQUIRE ACTIONS
					// lock is granted only if we have non-exception on acquire
					locks.put(lockInfo.getElementId(), lockInfo);
					incrementLockCount(lockInfo);

					// PersistentObject compatibility
					PersistentObject po = CoreHub.poFactory.createFromString(lockInfo.getElementStoreToString());
					if (po != null) {
						ElexisEventDispatcher.getInstance()
								.fire(new ElexisEvent(po, po.getClass(), ElexisEvent.EVENT_LOCK_AQUIRED));
						return lr;
					}
					// End

					Optional<Identifiable> identifiable = storeToStringService
							.loadFromString(lockInfo.getElementStoreToString());
					if (identifiable.isPresent()) {
						postEvent(ElexisEventTopics.EVENT_LOCK_AQUIRED, identifiable.get(), false);
					}

				}

				return lr;
			} catch (Exception e) {
				// if we have an exception here, our lock copies never get
				// deleted!!!
				String message = "Error trying to acquireOrReleaseLocks.";
				logger.error(message);
				ElexisEventDispatcher.fireElexisStatusEvent(new ElexisStatus(org.eclipse.core.runtime.Status.ERROR,
						CoreHub.PLUGIN_ID, ElexisStatus.CODE_NONE, message, e));
				return new LockResponse(LockResponse.Status.ERROR, lockRequest.getLockInfo());
			} finally {
				if (LockRequest.Type.RELEASE.equals(lockRequest.getRequestType())) {
					// RELEASE ACTIONS
					// releases are also to be performed on occurence of an
					// exception
					decrementLockCount(lockInfo);
					locks.remove(lockInfo.getElementId());

					PersistentObject po = CoreHub.poFactory.createFromString(lockInfo.getElementStoreToString());
					if (po != null) {
						ElexisEventDispatcher.getInstance()
								.fire(new ElexisEvent(po, po.getClass(), ElexisEvent.EVENT_LOCK_RELEASED));
					} else {
						// e4 should not be connected with else here,
						// but we have to avoid double calls
						Optional<Identifiable> identifiable = storeToStringService
								.loadFromString(lockInfo.getElementStoreToString());
						if (identifiable.isPresent()) {
							postEvent(ElexisEventTopics.EVENT_LOCK_RELEASED, identifiable.get(), false);
						}
					}

				}
			}
		}
	}

	private void incrementLockCount(LockInfo lockInfo) {
		Integer count = lockCount.get(lockInfo.getElementId());
		if (count == null) {
			count = Integer.valueOf(0);
		}
		lockCount.put(lockInfo.getElementId(), ++count);
		logger.debug("Increment to " + count + " locks on " + lockInfo.getElementId());
	}

	private void decrementLockCount(LockInfo lockInfo) {
		Integer count = lockCount.get(lockInfo.getElementId());
		if (count != null) {
			lockCount.put(lockInfo.getElementId(), --count);
			logger.debug("Decrement to " + count + " locks on " + lockInfo.getElementId());
			if (count < 1) {
				lockCount.remove(lockInfo.getElementId());
			}
		}
	}

	private Integer getCurrentLockCount(LockInfo lockInfo) {
		Integer count = lockCount.get(lockInfo.getElementId());
		if (count == null) {
			count = Integer.valueOf(0);
		}
		logger.debug("Got currently " + count + " locks on " + lockInfo.getElementId());
		return count;
	}

	@Override
	public boolean isLockedLocal(Object object) {
		if (object == null) {
			return false;
		}
		// handle not lockable local object (DTO)
		if (!isLockable(object)) {
			return true;
		}
		if (elexisServerService.isStandalone()) {
			return true;
		}
		// check local locks first
		if (locks.containsKey(getId(object))) {
			return true;
		}
		return false;
	}

	/**
	 * Only persistent objects with an id can be locked. Local data transfer objects
	 * (DTO) can not be locked.
	 *
	 * @param object
	 * @return
	 */
	private boolean isLockable(Object object) {
		return object instanceof Identifiable || object instanceof PersistentObject;
	}

	@Override
	public boolean isLocked(Object object) {
		if (object == null) {
			return false;
		}
		// handle not lockable local object (DTO)
		if (!isLockable(object)) {
			return true;
		}
		logger.debug("Checking lock on [" + object + "]");

		String storeToString = StoreToStringServiceHolder.getStoreToString(object);
		return isLocked(storeToString);
	}

	@Override
	public boolean isLocked(String storeToString) {
		if (storeToString == null) {
			return false;
		}

		IUser user = ContextServiceHolder.get().getActiveUser().orElse(null);
		LockInfo lockInfo = new LockInfo(storeToString, user.getId(), elexisServerService.getSystemUuid().toString(),
				contextService.getStationIdentifier(),
				configService.getLocal(Preferences.STATION_IDENT_TEXT, StringUtils.EMPTY));
		LockRequest lockRequest = new LockRequest(LockRequest.Type.INFO, lockInfo);
		return isLocked(lockRequest);
	}

	@Override
	public boolean isLocked(LockRequest lockRequest) {
		if (lockRequest == null || lockRequest.getLockInfo().getElementId() == null) {
			return false;
		}

		if (elexisServerService.isStandalone()) {
			return true;
		}
		// check local locks first
		if (locks.containsKey(lockRequest.getLockInfo().getElementId())) {
			return true;
		}

		try {
			return elexisServerService.isLocked(lockRequest);
		} catch (Exception e) {
			logger.error("Catched exception in isLocked: ", e);
			return false;
		}

	}

	@Override
	public List<LockInfo> getCopyOfAllHeldLocks() {
		Collection<LockInfo> values = locks.values();
		if (values.size() == 0) {
			return Collections.emptyList();
		}

		return new ArrayList<LockInfo>(values);
	}

	@Override
	public String getSystemUuid() {
		return elexisServerService.getSystemUuid().toString();
	}

	@Override
	public LockInfo getLockInfo(String storeToString) {
		String elementId = LockInfo.getElementId(storeToString);
		LockInfo lockInfo = locks.get(elementId);
		return lockInfo;
	}

	private class LockRefreshTask extends TimerTask {

		@Override
		public void run() {
			try {
				// verify and update the locks
				boolean publishUpdate = false;
				synchronized (locks) {
					List<String> lockKeys = new ArrayList<String>();
					lockKeys.addAll(locks.keySet());
					for (String key : lockKeys) {
						boolean success = elexisServerService.isLocked(new LockRequest(Type.INFO, locks.get(key)));
						if (!success) {
							publishUpdate = true;
							releaseLock(locks.get(key).getElementStoreToString());
						}
					}
				}
				if (publishUpdate) {
					ElexisEventDispatcher.getInstance()
							.fire(new ElexisEvent(null, LockInfo.class, ElexisEvent.EVENT_RELOAD));
				}
			} catch (Exception e) {
				LoggerFactory.getLogger(LockRefreshTask.class).error("Execution error", e);
			}
		}
	}

	private void postEvent(String topic, Object object, boolean synchronous) {
		if (synchronous) {
			contextService.postEvent(topic, object);
		} else {
			contextService.sendEvent(topic, object);
		}
	}

	@Override
	public void shutdown() {
		timer.cancel();
		if (elexisServerService != null) {
			InstanceStatus instanceStatus = elexisServerService.createInstanceStatus();
			instanceStatus.setState(STATE.SHUTTING_DOWN);
			elexisServerService.updateInstanceStatus(instanceStatus);
		}
	}
}
