package ch.elexis.core.services.holder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IMessageService;

@Component
public class MessageServiceHolder {
	private static IMessageService messageService;
	
	@Reference
	public void setAppointmentService(IMessageService messageService){
		MessageServiceHolder.messageService = messageService;
	}
	
	public static IMessageService get(){
		return messageService;
	}
}
