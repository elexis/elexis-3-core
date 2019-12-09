package ch.elexis.core.data.server;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.jdt.Nullable;
import ch.elexis.data.PersistentObject;

/**
 * Maps internal {@link ElexisEvent} instances to outgoing ElexisEvent instances sent to
 * Elexis-Server
 */
public class ServerEventMapper {
	
	/**
	 * Map an {@link ElexisEvent} to an Elexis-Server-Outgoing ElexisEvent
	 * 
	 * @param ee
	 * @return a remote event, or <code>null</code> if not supported
	 */
	public static @Nullable ch.elexis.core.common.ElexisEvent mapEvent(ElexisEvent ee){
		ch.elexis.core.common.ElexisEvent remoteEvent = new ch.elexis.core.common.ElexisEvent();
		String mapTopic = mapTopic(ee.getType());
		if (mapTopic == null) {
			return null;
		}
		remoteEvent.setTopic(mapTopic);
		
		PersistentObject object = ee.getObject();
		if (object != null) {
			remoteEvent.getProperties().put(ElexisEventTopics.PROPKEY_ID, object.getId());
			remoteEvent.getProperties().put(ElexisEventTopics.PROPKEY_CLASS,
				object.getClass().getName());
		}
		
		ContextServiceHolder.get().getActiveUser().ifPresent(
			u -> remoteEvent.getProperties().put(ElexisEventTopics.PROPKEY_USER, u.getId()));
		return remoteEvent;
	}
	
	private static String mapTopic(int type){
		switch (type) {
		case ElexisEvent.EVENT_CREATE:
			return ElexisEventTopics.PERSISTENCE_EVENT_CREATE;
		default:
			break;
		}
		return null;
	}
	
}
