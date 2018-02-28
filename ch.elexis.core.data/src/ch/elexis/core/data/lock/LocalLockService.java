package ch.elexis.core.data.lock;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eclipsesource.jaxrs.consumer.ConsumerFactory;

import ch.elexis.core.common.InstanceStatus;
import ch.elexis.core.common.InstanceStatus.STATE;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.server.ElexisServerInstanceService;
import ch.elexis.core.data.server.ElexisServerLockService;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.lock.ILocalLockService;
import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockRequest;
import ch.elexis.core.lock.types.LockRequest.Type;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.server.IInstanceService;
import ch.elexis.core.server.ILockService;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.User;

/**
 * ILocalLockService implementation. Managing locks of PersistentObjects.</br>
 * If the environment variable <b>ELEXIS_SERVER_REST_INTERFACE_URL</b> is set a connection to a
 * remote LockService will used internal.
 * 
 * @author marco
 * 
 */
public class LocalLockService implements ILocalLockService {
	
	private ILockService ils;
	private IInstanceService iis;
	private InstanceStatus inst;
	
	private final HashMap<String, Integer> lockCount = new HashMap<String, Integer>();
	private final HashMap<String, LockInfo> locks = new HashMap<String, LockInfo>();
	private boolean standalone = false;
	private Logger logger = LoggerFactory.getLogger(LocalLockService.class);
	
	/**
	 * A unique id for this instance of Elexis. Changes on every restart
	 */
	private static final UUID systemUuid = UUID.randomUUID();
	
	private Timer timer;
	
	/**
	 * Construct a new LocalLockService. Application code should access via
	 * {@link CoreHub#getLocalLockService()} and <b>NOT</b> create its own instance.
	 * 
	 */
	public LocalLockService(){
		ils = new DenyAllLockService();
		timer = new Timer();
		timer.schedule(new LockRefreshTask(), 10000, 10000);
		
		inst = new InstanceStatus();
		inst.setState(InstanceStatus.STATE.ACTIVE);
		inst.setUuid(getSystemUuid());
		inst.setVersion(CoreHub.readElexisBuildVersion());
		inst.setOperatingSystem(
			System.getProperty("os.name") + "/" + System.getProperty("os.version") + "/"
				+ System.getProperty("os.arch") + "/J" + System.getProperty("java.version"));
	}
	
	public void reconfigure(){
		final String restUrl =
			System.getProperty(ElexisSystemPropertyConstants.ELEXIS_SERVER_REST_INTERFACE_URL);
		if (restUrl != null && restUrl.length() > 0) {
			standalone = false;
			logger.info("Operating against elexis-server instance on " + restUrl);
			ils = new ElexisServerLockService(restUrl);
			iis = new ElexisServerInstanceService(restUrl);
			String identId = CoreHub.localCfg.get(Preferences.STATION_IDENT_ID, "");
			String identTxt = CoreHub.localCfg.get(Preferences.STATION_IDENT_TEXT, "");
			inst.setIdentifier(identTxt + " [" + identId + "]");
		} else {
			standalone = true;
			logger.info("Operating in stand-alone mode.");
		}
	}
	
	@Override
	public LockResponse releaseAllLocks(){
		if (standalone) {
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
	public LockResponse releaseLock(IPersistentObject po){
		if (po == null) {
			return LockResponse.DENIED(null);
		}
		logger.debug("Releasing lock on [" + po + "]");
		return releaseLock(po.storeToString());
	}
	
	@Override
	public LockResponse releaseLock(LockInfo lockInfo){
		if (lockInfo.getElementStoreToString() == null) {
			return LockResponse.DENIED(null);
		}
		logger.debug("Releasing lock on [" + lockInfo.getElementStoreToString() + "]");
		return releaseLock(lockInfo.getElementStoreToString());
	}
	
	private LockResponse releaseLock(String storeToString){
		User user = (User) ElexisEventDispatcher.getSelected(User.class);
		LockInfo lil = new LockInfo(storeToString, user.getId(), systemUuid.toString());
		LockRequest lockRequest = new LockRequest(LockRequest.Type.RELEASE, lil);
		return acquireOrReleaseLocks(lockRequest);
	}
	
	@Override
	public LockResponse acquireLockBlocking(IPersistentObject po, int secTimeout,
		IProgressMonitor monitor){
		if (po == null) {
			return LockResponse.DENIED(null);
		}
		if (monitor != null) {
			monitor.beginTask("Acquiring Lock for [" + po.getLabel() + "]", (secTimeout * 10) + 1);
		}
		logger.debug("Acquiring lock blocking on [" + po + "]");
		String storeToString = po.storeToString();
		
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
	public LockResponse acquireLock(IPersistentObject po){
		if (po == null) {
			return LockResponse.DENIED(null);
		}
		logger.debug("Acquiring lock on [" + po + "]");
		LockResponse lr = acquireLock(po.storeToString());
		
		if (lr.getStatus() == LockResponse.Status.ERROR) {
			logger.warn("LockResponse ERROR");
		}
		
		return lr;
	}
	
	private LockResponse acquireLock(String storeToString){
		if (storeToString == null) {
			return LockResponse.DENIED(null);
		}
		
		User user = (User) ElexisEventDispatcher.getSelected(User.class);
		LockInfo lockInfo = new LockInfo(storeToString, user.getId(), systemUuid.toString());
		LockRequest lockRequest = new LockRequest(LockRequest.Type.ACQUIRE, lockInfo);
		return acquireOrReleaseLocks(lockRequest);
	}
	
	@Override
	public LockResponse acquireOrReleaseLocks(LockRequest lockRequest){
		if (standalone) {
			return LockResponse.OK(lockRequest.getLockInfo());
		}
		
		if (ils == null) {
			String message =
				"System not configured for standalone mode, and elexis-server not available!";
			logger.error(message);
			ElexisEventDispatcher
				.fireElexisStatusEvent(new ElexisStatus(org.eclipse.core.runtime.Status.ERROR,
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
			if (LockRequest.Type.RELEASE == lockRequest.getRequestType()
				&& getCurrentLockCount(lockInfo) > 1) {
				decrementLockCount(lockInfo);
				return LockResponse.OK(lockRequest.getLockInfo());
			}
			// TODO should we release all locks on acquiring a new one?
			// if yes, this has to be dependent upon the strategy
			try {
				if (LockRequest.Type.RELEASE == lockRequest.getRequestType()) {
					PersistentObject po =
						CoreHub.poFactory.createFromString(lockInfo.getElementStoreToString());
					if (po != null) {
						ElexisEventDispatcher.getInstance().fire(new ElexisEvent(po, po.getClass(),
							ElexisEvent.EVENT_LOCK_PRERELEASE, ElexisEvent.PRIORITY_SYNC));
					}
				}
				
				LockResponse lr = ils.acquireOrReleaseLocks(lockRequest);
				if (!lr.isOk()) {
					return lr;
				}
				
				if (LockRequest.Type.ACQUIRE == lockRequest.getRequestType()) {
					// ACQUIRE ACTIONS
					// lock is granted only if we have non-exception on acquire
					locks.put(lockInfo.getElementId(), lockInfo);
					incrementLockCount(lockInfo);
					PersistentObject po =
						CoreHub.poFactory.createFromString(lockInfo.getElementStoreToString());
					if (po != null) {
						ElexisEventDispatcher.getInstance().fire(
							new ElexisEvent(po, po.getClass(), ElexisEvent.EVENT_LOCK_AQUIRED));
					}
				}
				
				return lr;
			} catch (Exception e) {
				// if we have an exception here, our lock copies never get
				// deleted!!!
				String message = "Error trying to acquireOrReleaseLocks.";
				logger.error(message);
				ElexisEventDispatcher
					.fireElexisStatusEvent(new ElexisStatus(org.eclipse.core.runtime.Status.ERROR,
						CoreHub.PLUGIN_ID, ElexisStatus.CODE_NONE, message, e));
				return new LockResponse(LockResponse.Status.ERROR, lockRequest.getLockInfo());
			} finally {
				if (LockRequest.Type.RELEASE.equals(lockRequest.getRequestType())) {
					// RELEASE ACTIONS
					// releases are also to be performed on occurence of an
					// exception
					decrementLockCount(lockInfo);
					locks.remove(lockInfo.getElementId());
					
					PersistentObject po =
						CoreHub.poFactory.createFromString(lockInfo.getElementStoreToString());
					if (po != null) {
						ElexisEventDispatcher.getInstance().fire(
							new ElexisEvent(po, po.getClass(), ElexisEvent.EVENT_LOCK_RELEASED));
					}
				}
			}
		}
	}
	
	private void incrementLockCount(LockInfo lockInfo){
		Integer count = lockCount.get(lockInfo.getElementId());
		if (count == null) {
			count = new Integer(0);
		}
		lockCount.put(lockInfo.getElementId(), ++count);
		logger.debug("Increment to " + count + " locks on " + lockInfo.getElementId());
	}
	
	private void decrementLockCount(LockInfo lockInfo){
		Integer count = lockCount.get(lockInfo.getElementId());
		if (count != null) {
			lockCount.put(lockInfo.getElementId(), --count);
			logger.debug("Decrement to " + count + " locks on " + lockInfo.getElementId());
			if (count < 1) {
				lockCount.remove(lockInfo.getElementId());
			}
		}
	}
	
	private Integer getCurrentLockCount(LockInfo lockInfo){
		Integer count = lockCount.get(lockInfo.getElementId());
		if (count == null) {
			count = new Integer(0);
		}
		logger.debug("Got currently " + count + " locks on " + lockInfo.getElementId());
		return count;
	}
	
	@Override
	public boolean isLockedLocal(IPersistentObject po){
		if (po == null) {
			return false;
		}
		
		if (standalone) {
			return true;
		}
		// check local locks first
		if (locks.containsKey(po.getId())) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isLocked(IPersistentObject po){
		if (po == null) {
			return false;
		}
		logger.debug("Checking lock on [" + po + "]");
		
		User user = (User) ElexisEventDispatcher.getSelected(User.class);
		LockInfo lockInfo = new LockInfo(po.storeToString(), user.getId(), systemUuid.toString());
		LockRequest lockRequest = new LockRequest(LockRequest.Type.INFO, lockInfo);
		
		return isLocked(lockRequest);
	}
	
	@Override
	public boolean isLocked(LockRequest lockRequest){
		if (lockRequest == null || lockRequest.getLockInfo().getElementId() == null) {
			return false;
		}
		
		if (standalone) {
			return true;
		}
		// check local locks first
		if (locks.containsKey(lockRequest.getLockInfo().getElementId())) {
			return true;
		}
		
		try {
			return ils.isLocked(lockRequest);
		} catch (Exception e) {
			logger.error("Catched exception in isLocked: ", e);
			return false;
		}
		
	}
	
	@Override
	public List<LockInfo> getCopyOfAllHeldLocks(){
		Collection<LockInfo> values = locks.values();
		if (values.size() == 0) {
			return Collections.emptyList();
		}
		
		return new ArrayList<LockInfo>(values);
	}
	
	@Override
	public String getSystemUuid(){
		return systemUuid.toString();
	}
	
	@Override
	public LockInfo getLockInfo(String storeToString){
		String elementId = LockInfo.getElementId(storeToString);
		LockInfo lockInfo = locks.get(elementId);
		return lockInfo;
	}
	
	private class LockRefreshTask extends TimerTask {
		private ILockService restService;
		
		@Override
		public void run(){
			try {
				final String restUrl = System
					.getProperty(ElexisSystemPropertyConstants.ELEXIS_SERVER_REST_INTERFACE_URL);
				if (restUrl != null && !restUrl.isEmpty()) {
					final String testRestUrl = restUrl + "/elexis/lockservice/lockInfo";
					// if service is available but we are not using it -> use it
					// if service not available but we are using it -> dont use it
					if (testRestUrl(testRestUrl) && ils instanceof DenyAllLockService
						&& restService != null) {
						ils = restService;
						iis = ConsumerFactory.createConsumer(restUrl, IInstanceService.class);
						// publish change
						ElexisEventDispatcher.getInstance().fire(new ElexisEvent(null,
							ILocalLockService.class, ElexisEvent.EVENT_RELOAD));
					} else if (!testRestUrl(testRestUrl) && !(ils instanceof DenyAllLockService)) {
						restService = ils;
						iis = null;
						ils = new DenyAllLockService();
						// publish change
						ElexisEventDispatcher.getInstance().fire(new ElexisEvent(null,
							ILocalLockService.class, ElexisEvent.EVENT_RELOAD));
					}
				}
				
				if (standalone) {
					return;
				}
				
				if (iis != null) {
					User u = (User) ElexisEventDispatcher.getSelected(User.class);
					inst.setActiveUser((u != null) ? u.getId() : "NO USER ACTIVE");
					iis.updateStatus(inst);
				}
				
				// verify and update the locks
				boolean publishUpdate = false;
				synchronized (locks) {
					List<String> lockKeys = new ArrayList<String>();
					lockKeys.addAll(locks.keySet());
					for (String key : lockKeys) {
						boolean success = ils.isLocked(new LockRequest(Type.INFO, locks.get(key)));
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
		
		private boolean testRestUrl(String restUrl){
			try {
				URL url = new URL(restUrl);
				HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
				urlConn.connect();
				
				return (urlConn.getResponseCode() >= 200 && urlConn.getResponseCode() < 300);
			} catch (IOException e) {
				return false;
			}
		}
	}
	
	private class DenyAllLockService implements ILockService {
		
		@Override
		public LockResponse acquireOrReleaseLocks(LockRequest request){
			return LockResponse
				.DENIED(getLockInfo(request.getLockInfo().getElementStoreToString()));
		}
		
		@Override
		public boolean isLocked(LockRequest request){
			return false;
		}
		
		@Override
		public LockInfo getLockInfo(String storeToString){
			return new LockInfo(storeToString, "LockService", "DenyAllLockService");
		}
		
	}
	
	@Override
	public Status getStatus(){
		if (standalone) {
			return Status.STANDALONE;
		} else if (ils == null || ils instanceof DenyAllLockService) {
			return Status.LOCAL;
		}
		return Status.REMOTE;
	}
	
	@Override
	public void shutdown(){
		timer.cancel();
		if (iis != null) {
			inst.setState(STATE.SHUTTING_DOWN);
			iis.updateStatus(inst);
		}
	}
}
