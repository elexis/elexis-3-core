package ch.elexis.core.services.internal;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.osgi.service.component.annotations.Component;

import ch.elexis.core.model.IMessage;
import ch.elexis.core.model.message.TransientMessage;
import ch.elexis.core.services.IMessageTransporter;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.status.ObjectStatus;

/**
 * Transport the message via the Elexis internal (database-based) message
 * system.
 */
@Component
public class InternalDatabaseMessageTransporter implements IMessageTransporter {

	@Override
	public IStatus send(TransientMessage message) {

		IMessage idbMessage = CoreModelServiceHolder.get().create(IMessage.class);
		String sender = StringUtils.truncate(message.getSender(), 25);
		idbMessage.setSender(sender);
		idbMessage.setMessageText(message.getMessageText());
		idbMessage.setMessageCodes(message.getMessageCodes());
		idbMessage.setMessagePriority(message.getMessagePriority());
		idbMessage.setCreateDateTime(message.getCreateDateTime());
		idbMessage.setSenderAcceptsAnswer(message.isSenderAcceptsAnswer());
		// internaldb supports only a single receiver
		idbMessage.addReceiver(message.getReceiver());

		try {
			CoreModelServiceHolder.get().save(idbMessage);
			return ObjectStatus.OK_STATUS(idbMessage.getId(), null);
		} catch (IllegalStateException e) {
			return ObjectStatus.ERROR_STATUS("Could not save message", e);
		}
	}

	@Override
	public String getUriScheme() {
		return "internaldb";
	}

	@Override
	public boolean isExternal() {
		return false;
	}

}
