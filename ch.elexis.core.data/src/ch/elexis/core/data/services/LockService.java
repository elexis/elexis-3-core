package ch.elexis.core.data.services;

import java.util.Collections;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.data.PersistentObject;
import info.elexis.server.elexis.common.ILockService;

public class LockService {
	
	private static ILockService ils;
	
	private HashSet<String> locks = new HashSet<String>();
	
	private final boolean standalone;
	
	private Logger log = LoggerFactory.getLogger(LockService.class);
	
	public LockService(){
		if (System.getProperty(ElexisSystemPropertyConstants.STANDALONE_MODE) != null) {
			standalone = true;
			log.info("Operating in stand-alone mode.");
		} else {
			standalone = false;
			// init remote locking service
		}
	}
	
	public boolean acquireLock(String storeToString, String id){
		if (standalone) {
			return true;
		}
		
		synchronized (locks) {
			if (locks.contains(storeToString)) {
				return true;
			}
			
			if (!ils.acquireLocks(Collections.singleton(storeToString), CoreHub.actUser.getId())) {
				return false;
			}
			
			// For first version we lock on patient, resp. whole domain
			// so we should add all dependendent locking elements
			locks.add(storeToString);
			PersistentObject po = CoreHub.poFactory.createFromString(storeToString);
			ElexisEventDispatcher.getInstance()
				.fire(new ElexisEvent(po, po.getClass(), ElexisEvent.EVENT_LOCK_GRANTED));
		}
		
		return true;
	}
	
	public boolean ownsLock(String storeToString){
		if (standalone) {
			return true;
		}
		
		return locks.contains(storeToString);
	}
}
