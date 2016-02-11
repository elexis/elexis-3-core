package ch.elexis.core.data.lock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eclipsesource.jaxrs.consumer.ConsumerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.status.ElexisStatus;
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

	public LockService(BundleContext context) {
		final String restUrl = System.getProperty(ElexisSystemPropertyConstants.ELEXIS_SERVER_REST_INTERFACE_URL);
		if (restUrl != null) {
			standalone = false;
			log.info("Operating against elexis-server instance on " + restUrl);
			ils = ConsumerFactory.createConsumer(restUrl, ILockService.class);
			// TODO validate correct location
		} else {
			standalone = true;
			log.info("Operating in stand-alone mode.");
		}
	}

	public boolean acquireLock(String storeToString) {
		User user = (User) ElexisEventDispatcher.getSelected(User.class);
		List<LockInfo> lil = LockByPatientStrategy.createLockInfoList(storeToString, user.getId());
		return acquireLock(lil);
	}

	public boolean acquireLock(List<LockInfo> lockInfos) {
		if (standalone) {
			return true;
		}

		synchronized (locks) {
			// does the requested lock match the cache on our side?
			List<String> elementIds = lockInfos.stream().map(l -> l.getElementId()).collect(Collectors.toList());
			if (locks.keySet().containsAll(elementIds)) {
				return true;
			}

			// TODO should we release all locks on acquiring a new one?
			// if yes, this has to be dependent upon the strategy

			// TODO
			// what if lock service is gone???
			// remove all current locks??
			if (ils == null) {
				log.error("System not configured for standalone mode, and elexis-server not available!");
				return false;
			}
			LockRequest lockRequest = new LockRequest(LockRequest.Type.ACQUIRE, lockInfos);
			if (!ils.acquireOrReleaseLocks(lockRequest)) {
				return false;
			}

			lockInfos.stream().forEach(l -> locks.put(l.getElementId(), l));

			for (LockInfo li : lockInfos) {
				PersistentObject po = CoreHub.poFactory.createFromString(li.getElementStoreToString());
				ElexisEventDispatcher.getInstance()
						.fire(new ElexisEvent(po, po.getClass(), ElexisEvent.EVENT_LOCK_AQUIRED));
			}
			return true;
		}
	}

	public boolean releaseAllLocks() {
		List<LockInfo> lockList = new ArrayList<LockInfo>(locks.values());
		if (lockList.size() == 0) {
			return true;
		}

		synchronized (locks) {
			LockRequest lockRequest = new LockRequest(LockRequest.Type.RELEASE, lockList);
			if (!ils.acquireOrReleaseLocks(lockRequest)) {
				return false;
			}
			locks.clear();
			for (LockInfo li : lockList) {
				PersistentObject po = CoreHub.poFactory.createFromString(li.getElementStoreToString());
				ElexisEventDispatcher.getInstance()
						.fire(new ElexisEvent(po, po.getClass(), ElexisEvent.EVENT_LOCK_RELEASED));
			}
			return true;
		}
	}

	public boolean ownsLock(String storeToString) {
		if (standalone) {
			return true;
		}

		String elementId = LockInfo.getElementId(storeToString);
		return locks.containsKey(elementId);
	}

}
