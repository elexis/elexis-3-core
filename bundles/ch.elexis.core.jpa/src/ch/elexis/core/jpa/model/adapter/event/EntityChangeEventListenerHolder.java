package ch.elexis.core.jpa.model.adapter.event;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component
public class EntityChangeEventListenerHolder {
	
	private static EntityChangeEventListener entityChangeListener;
	
	@Reference
	public void setEntityChangeEventListener(EntityChangeEventListener entityChangeListener){
		EntityChangeEventListenerHolder.entityChangeListener = entityChangeListener;
	}
	
	public static EntityChangeEventListener get(){
		if (entityChangeListener == null) {
			throw new IllegalStateException("No EntityChangeEventListener available");
		}
		return entityChangeListener;
	}
}
