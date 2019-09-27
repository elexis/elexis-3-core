package ch.elexis.core.services;

import org.eclipse.core.runtime.IStatus;
import org.osgi.service.component.annotations.Component;

import ch.elexis.core.model.IMessage;
import ch.elexis.core.model.message.TransientMessage;
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
		// TODO copy all
		
//		IMessage persistedMessage = CoreModelServiceHolder.get().create(IMessage.class);
		
//		boolean result = CoreModelServiceHolder.get().save(message);
//		if(result) {
//			return Status.OK_STATUS;
//		}
		return ObjectStatus.ERROR_STATUS(message.getId());
	}

	@Override
	public int getDefaultPriority(){
		return 0;
	}

	@Override
	public String getId(){
		return "internaldatabase";
	}
	
}
