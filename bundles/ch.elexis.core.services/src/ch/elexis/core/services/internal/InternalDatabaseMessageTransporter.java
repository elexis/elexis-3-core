package ch.elexis.core.services.internal;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMessage;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.message.TransientMessage;
import ch.elexis.core.services.IMessageTransporter;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.status.ObjectStatus;

/**
 * Transport the message via the Elexis internal (database-based) message
 * system.
 */
@Component
public class InternalDatabaseMessageTransporter implements IMessageTransporter {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@Override
	public IStatus send(TransientMessage message) {

		String receiver = message.getReceiver();
		// is receiver a user? map to assigned contact
		Optional<IUser> user = coreModelService.load(receiver, IUser.class);
		if (user.isPresent()) {
			IContact assignedContact = user.get().getAssignedContact();
			if (assignedContact != null) {
				receiver = assignedContact.getId();
			}
		}
		// load contact, and check if its a user
		IContact contact = coreModelService.load(receiver, IContact.class).orElse(null);
		if (contact == null || !contact.isUser()) {
			return new Status(Status.CANCEL, getClass(), "invalid receiver, or receiver is not user");
		}

		IMessage idbMessage = prepareMessage(receiver, message);

		try {
			CoreModelServiceHolder.get().save(idbMessage);
			return ObjectStatus.OK_STATUS(idbMessage.getId(), null);
		} catch (IllegalStateException e) {
			return ObjectStatus.ERROR_STATUS("Could not save message", e);
		}
	}

	private IMessage prepareMessage(String receiver, TransientMessage message) {
		IMessage idbMessage = coreModelService.create(IMessage.class);
		String sender = StringUtils.truncate(message.getSender(), 25);
		idbMessage.setSender(sender);
		idbMessage.setMessageText(message.getMessageText());
		idbMessage.setMessageCodes(message.getMessageCodes());
		idbMessage.setMessagePriority(message.getMessagePriority());
		idbMessage.setCreateDateTime(message.getCreateDateTime());
		idbMessage.setSenderAcceptsAnswer(message.isSenderAcceptsAnswer());
		// internaldb supports only a single receiver
		idbMessage.addReceiver(receiver);
		return idbMessage;
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
