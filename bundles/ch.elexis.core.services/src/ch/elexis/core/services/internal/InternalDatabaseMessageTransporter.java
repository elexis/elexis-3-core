package ch.elexis.core.services.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.service.component.annotations.Component;

import ch.elexis.core.model.IMessage;
import ch.elexis.core.model.message.TransientMessage;
import ch.elexis.core.services.IMessageTransporter;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.status.ObjectStatus;

/**
 * Transport the message via the Elexis internal (database-based) message system.
 */
@Component
public class InternalDatabaseMessageTransporter implements IMessageTransporter {
	
	@Override
	public IStatus send(TransientMessage message){
		
		IMessage idbMessage = CoreModelServiceHolder.get().create(IMessage.class);
		idbMessage.setSender(message.getSender());
		idbMessage.setMessageText(message.getMessageText());
		idbMessage.setMessageCodes(message.getMessageCodes());
		idbMessage.setMessagePriority(message.getMessagePriority());
		idbMessage.setCreateDateTime(message.getCreateDateTime());
		idbMessage.setSenderAcceptsAnswer(message.isSenderAcceptsAnswer());
		
		boolean save = CoreModelServiceHolder.get().save(idbMessage);
		if (save) {
			return Status.OK_STATUS;
		}
		
		return ObjectStatus.ERROR_STATUS(idbMessage.getId());
	}
	
	@Override
	public String getUriScheme(){
		return "internaldb";
	}
	
	@Override
	public boolean isExternal(){
		return false;
	}
	
}
