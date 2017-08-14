package ch.elexis.core.ui.documents.service;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import at.medevit.elexis.outbox.model.IOutboxElementService;

@Component
public class OutboxElementServiceHolder {
	private static IOutboxElementService outboxElementService;
	
	@Reference
	public void setOutboxElementService(IOutboxElementService outboxElementService){
		OutboxElementServiceHolder.outboxElementService = outboxElementService;
	}
	
	public void unsetOutboxElementService(IOutboxElementService outboxElementService){
		OutboxElementServiceHolder.outboxElementService = null;
	}
	
	public static IOutboxElementService getService(){
		if (outboxElementService == null) {
			throw new IllegalStateException("No IOutboxElementService available");
		}
		return outboxElementService;
	}
}
