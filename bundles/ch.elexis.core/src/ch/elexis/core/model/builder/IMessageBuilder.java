package ch.elexis.core.model.builder;

import java.time.LocalDateTime;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMessage;
import ch.elexis.core.services.IModelService;

public class IMessageBuilder extends AbstractBuilder<IMessage> {

	public IMessageBuilder(IModelService modelService, IContact sender, IContact receiver) {
		this(modelService, sender.getId(), receiver.getId());
	}

	public IMessageBuilder(IModelService modelService, String sender, IContact receiver) {
		this(modelService, sender, receiver.getId());
	}

	public IMessageBuilder(IModelService modelService, String sender, String receiver) {
		this(modelService, sender, new String[] { receiver });
	}

	public IMessageBuilder(IModelService modelService, String sender, String... receiver) {
		super(modelService);

		object = modelService.create(IMessage.class);
		object.setSender(sender);
		object.setDeleted(false);
		object.setCreateDateTime(LocalDateTime.now());
		for (String iMessageParty : receiver) {
			object.addReceiver(iMessageParty);
		}
	}

}
