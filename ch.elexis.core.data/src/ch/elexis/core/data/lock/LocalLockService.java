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

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.lock.ILocalLockService;
import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockRequest;
import ch.elexis.core.lock.types.LockRequest.Type;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IPersistentObject;
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
	}
	
	public void reconfigure(){
		final String restUrl =
			System.getProperty(ElexisSystemPropertyConstants.ELEXIS_SERVER_REST_INTERFACE_URL);
		if (restUrl != null && restUrl.length() > 0) {
			standalone = false;
			logger.info("Operating against elexis-server instance on " + restUrl);
			ils = ConsumerFactory.createConsumer(restUrl, ILockService.class);
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
			monitor.beginTask("Acquiring Lock for [" + po.getLabel() + "]", secTimeout * 2);
		}
		logger.debug("Acquiring lock blocking on [" + po + "]");
		String storeToString = po.storeToString();
		
		LockResponse response = acquireLock(storeToString);
		int sleptMilli = 0;
		while (!response.isOk()) {
			try {
				Thread.sleep(1000);
				sleptMilli += 1000;
				response = acquireLock(storeToString);
				if (response.getStatus() == LockResponse.Status.DENIED_PERMANENT) {
					return response;
				}
				if (sleptMilli > (secTimeout * 1000)) {
					return response;
				}
				// update monitor
				if (monitor != null) {
					monitor.worked(1);
					if (monitor.isCanceled()) {
						return LockResponse.DENIED(response.getLockInfos());
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
			return LockResponse.OK;
		}
		
		if (ils == null) {
			String message =
				"System not configured for standalone mode, and elexis-server not available!";
			logger.error(message);
			ElexisEventDispatcher
				.fireElexisStatusEvent(new ElexisStatus(org.eclipse.core.runtime.Status.ERROR,
					CoreHub.PLUGIN_ID, ElexisStatus.CODE_NONE, message, null));
			return LockResponse.ERROR;
		}
		
		LockInfo lockInfo = lockRequest.getLockInfo();
		
		synchronized (locks) {
			// does the requested lock match the cache on our side?
			if (LockRequest.Type.ACQUIRE == lockRequest.getRequestType()
				&& locks.keySet().contains(lockInfo.getElementId())) {
				return LockResponse.OK;
			}
			
			// TODO should we release all locks on acquiring a new one?
			// if yes, this has to be dependent upon the strategy
			try {
				if(LockRequest.Type.RELEASE == lockRequest.getRequestType()) {
					PersistentObject po =
							CoreHub.poFactory.createFromString(lockInfo.getElementStoreToString());
					ElexisEventDispatcher.getInstance().fire(new ElexisEvent(po, po.getClass(),
						ElexisEvent.EVENT_LOCK_PRERELEASE, ElexisEvent.PRIORITY_SYNC));
				}
				
				LockResponse lr = ils.acquireOrReleaseLocks(lockRequest);
				if (!lr.isOk()) {
					return lr;
				}
				
				if (LockRequest.Type.ACQUIRE == lockRequest.getRequestType()) {
					// ACQUIRE ACTIONS
					// lock is granted only if we have non-exception on acquire
					locks.put(lockInfo.getElementId(), lockInfo);
					
					PersistentObject po =
						CoreHub.poFactory.createFromString(lockInfo.getElementStoreToString());
					ElexisEventDispatcher.getInstance()
						.fire(new ElexisEvent(po, po.getClass(), ElexisEvent.EVENT_LOCK_AQUIRED));
					
				}
			} catch (Exception e) {
				// if we have an exception here, our lock copies never get
				// deleted!!!
				String message = "Error trying to acquireOrReleaseLocks.";
				logger.error(message);
				ElexisEventDispatcher
					.fireElexisStatusEvent(new ElexisStatus(org.eclipse.core.runtime.Status.ERROR,
						CoreHub.PLUGIN_ID, ElexisStatus.CODE_NONE, message, e));
				return LockResponse.ERROR;
			} finally {
				if (LockRequest.Type.RELEASE.equals(lockRequest.getRequestType())) {
					// RELEASE ACTIONS
					// releases are also to be performed on occurence of an
					// exception
					
					locks.remove(lockInfo.getElementId());
					
					PersistentObject po =
							CoreHub.poFactory.createFromString(lockInfo.getElementStoreToString());
					ElexisEventDispatcher.getInstance()
						.fire(new ElexisEvent(po, po.getClass(), ElexisEvent.EVENT_LOCK_RELEASED));
				}
			}
			
			return LockResponse.OK;
		}
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
					// if service is available but we are not using it -> use it
					// if service not available but we are using it -> dont use it
					if (testRestUrl(restUrl) && ils instanceof DenyAllLockService
						&& restService != null) {
						ils = restService;
						// publish change
						ElexisEventDispatcher.getInstance().fire(new ElexisEvent(null,
							ILocalLockService.class, ElexisEvent.EVENT_RELOAD));
					} else if (!testRestUrl(restUrl) && !(ils instanceof DenyAllLockService)) {
						restService = ils;
						ils = new DenyAllLockService();
						// publish change
						ElexisEventDispatcher.getInstance().fire(new ElexisEvent(null,
							ILocalLockService.class, ElexisEvent.EVENT_RELOAD));
					}
				}
				
				if (standalone) {
					return;
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
				
				return HttpURLConnection.HTTP_OK == urlConn.getResponseCode();
			} catch (IOException e) {
				return false;
			}
		}
	}
	
	private class DenyAllLockService implements ILockService {
		
		@Override
		public LockResponse acquireOrReleaseLocks(LockRequest request){
			return LockResponse.DENIED(request.getLockInfo());
		}
		
		@Override
		public boolean isLocked(LockRequest request){
			return false;
		}
		
		@Override
		public LockInfo getLockInfo(String storeToString){
			return null;
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
}
