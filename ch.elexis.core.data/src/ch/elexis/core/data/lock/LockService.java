package ch.elexis.core.data.lock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eclipsesource.jaxrs.consumer.ConsumerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.data.events.ElexisEventListenerImpl;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.User;
import info.elexis.server.elexis.common.jaxrs.ILockService;
import info.elexis.server.elexis.common.types.LockInfo;
import info.elexis.server.elexis.common.types.LockRequest;

public class LockService {

	private static ILockService ils;

	private HashMap<String, LockInfo> locks = new HashMap<String, LockInfo>();
	private final boolean standalone;
	private Logger log = LoggerFactory.getLogger(LockService.class);

	/**
	 * A unique id for this instance of Elexis. Changes on every restart
	 */
	private static final UUID systemUuid = UUID.randomUUID();

	private ElexisEventListener eeli_pat = new ElexisEventListenerImpl(Patient.class, ElexisEvent.EVENT_DESELECTED) {
		public void run(ElexisEvent ev) {
			if (ev.getObject() == null) {
				return;
			}
			String sts = ev.getObject().storeToString();
			if (sts != null) {
				if(ownsLock(sts)) {
					releaseLock(sts);
				}
			}
		};
	};

	public LockService(BundleContext context) {
		final String restUrl = System.getProperty(ElexisSystemPropertyConstants.ELEXIS_SERVER_REST_INTERFACE_URL);
		if (restUrl != null) {
			standalone = false;
			log.info("Operating against elexis-server instance on " + restUrl);
			ils = ConsumerFactory.createConsumer(restUrl, ILockService.class);
		} else {
			standalone = true;
			log.info("Operating in stand-alone mode.");
		}
		ElexisEventDispatcher.getInstance().addListeners(eeli_pat);
	}

	public static String getSystemuuid() {
		return systemUuid.toString();
	}

	public boolean acquireLock(PersistentObject po) {
		if (po == null) {
			return false;
		}
		return acquireLock(po.storeToString());
	}

	public boolean acquireLock(String storeToString) {
		if (storeToString == null) {
			return false;
		}

		User user = (User) ElexisEventDispatcher.getSelected(User.class);
		return acquireLock(Collections.singletonList(new LockInfo(storeToString, user.getId(), systemUuid.toString())));
	}

	public boolean acquireLock(List<LockInfo> lockInfos) {
		if (standalone) {
			return true;
		}

		LockRequest lockRequest = new LockRequest(LockRequest.Type.ACQUIRE, lockInfos);
		return acquireOrReleaseLock(lockRequest);
	}

	/**
	 * 
	 * @param storeToString
	 *            if <code>null</code> returns false
	 * @return
	 */
	public boolean ownsLock(String storeToString) {
		if (storeToString == null) {
			return false;
		}

		if (standalone) {
			return true;
		}

		String elementId = LockInfo.getElementId(storeToString);
		return locks.containsKey(elementId);
	}

	public boolean releaseLock(String storeToString) {
		User user = (User) ElexisEventDispatcher.getSelected(User.class);
		List<LockInfo> lil = LockByPatientStrategy.createLockInfoList(storeToString, user.getId(),
				systemUuid.toString());
		LockRequest lockRequest = new LockRequest(LockRequest.Type.RELEASE, lil);
		return acquireOrReleaseLock(lockRequest);
	}

	public boolean releaseAllLocks() {
		if (standalone) {
			return true;
		}

		List<LockInfo> lockList = new ArrayList<LockInfo>(locks.values());
		LockRequest lockRequest = new LockRequest(LockRequest.Type.RELEASE, lockList);
		return acquireOrReleaseLock(lockRequest);
	}

	private boolean acquireOrReleaseLock(LockRequest lockRequest) {
		if (standalone) {
			return true;
		}

		if (ils == null) {
			String message = "System not configured for standalone mode, and elexis-server not available!";
			log.error(message);
			ElexisEventDispatcher.fireElexisStatusEvent(
					new ElexisStatus(Status.ERROR, CoreHub.PLUGIN_ID, ElexisStatus.CODE_NONE, message, null));
			return false;
		}

		List<LockInfo> lockInfos = lockRequest.getLockInfos();
		List<String> elementIds = lockInfos.stream().map(l -> l.getElementId()).collect(Collectors.toList());

		synchronized (locks) {
			// does the requested lock match the cache on our side?
			if (LockRequest.Type.ACQUIRE == lockRequest.getRequestType() && locks.keySet().containsAll(elementIds)) {
				return true;
			}

			// TODO should we release all locks on acquiring a new one?
			// if yes, this has to be dependent upon the strategy
			try {
				if (!ils.acquireOrReleaseLocks(lockRequest)) {
					return false;
				}

				if (LockRequest.Type.ACQUIRE == lockRequest.getRequestType()) {
					// ACQUIRE ACTIONS
					// lock is granted only if we have non-exception on acquire
					lockInfos.stream().forEach(l -> locks.put(l.getElementId(), l));

					for (LockInfo li : lockInfos) {
						PersistentObject po = CoreHub.poFactory.createFromString(li.getElementStoreToString());
						ElexisEventDispatcher.getInstance()
								.fire(new ElexisEvent(po, po.getClass(), ElexisEvent.EVENT_LOCK_AQUIRED));
					}
				}
			} catch (Exception e) {
				// if we have an exception here, our lock copies never get
				// deleted!!!
				String message = "Error trying to acquireOrReleaseLocks.";
				log.error(message);
				ElexisEventDispatcher.fireElexisStatusEvent(
						new ElexisStatus(Status.ERROR, CoreHub.PLUGIN_ID, ElexisStatus.CODE_NONE, message, e));
				return false;
			} finally {
				if (LockRequest.Type.RELEASE.equals(lockRequest.getRequestType())) {
					// RELEASE ACTIONS
					// releases are also to be performed on occurence of an
					// exception
					lockInfos.stream().forEach(l -> locks.remove(l.getElementId()));

					for (LockInfo li : lockInfos) {
						PersistentObject po = CoreHub.poFactory.createFromString(li.getElementStoreToString());
						ElexisEventDispatcher.getInstance()
								.fire(new ElexisEvent(po, po.getClass(), ElexisEvent.EVENT_LOCK_RELEASED));
					}
				}
			}

			return true;
		}
	}

	public boolean releaseLock(PersistentObject po) {
		if (po == null) {
			return false;
		}
		return releaseLock(po.storeToString());
	}

	// /**
	// * Query whether the given object is currently locked by someone else
	// (that is
	// * an acquireLock would fail if we return true here)
	// * @param storeToString
	// * @return
	// */
	// public boolean isLockedBySomeoneElse(String storeToString) {
	// if(standalone) {
	// return false;
	// }
	// try {
	// return ils.isLocked(storeToString);
	// } catch (Exception e) {
	// String message = "Error trying to acquireOrReleaseLocks.";
	// log.error(message);
	// ElexisEventDispatcher.fireElexisStatusEvent(
	// new ElexisStatus(Status.ERROR, CoreHub.PLUGIN_ID, ElexisStatus.CODE_NONE,
	// message, e));
	// return true;
	// }
	// }

}
