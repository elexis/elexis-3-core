package ch.elexis.core.model.builder;

import java.time.LocalDateTime;

import ch.elexis.core.model.IMessage;
import ch.elexis.core.model.IMessageParty;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.message.MessageParty;
import ch.elexis.core.services.IModelService;

public class IMessageBuilder extends AbstractBuilder<IMessage> {
	
	public IMessageBuilder(IModelService modelService, IUser sender, IUser receiver){
		this(modelService, new MessageParty(sender.getId(), 0),
			new MessageParty(receiver.getId(), 0));
	}
	
	public IMessageBuilder(IModelService modelService, IMessageParty sender, IUser receiver){
		this(modelService, sender, new MessageParty(receiver.getId(), 0));
	}
	
	public IMessageBuilder(IModelService modelService, IMessageParty sender,
		IMessageParty receiver){
		this(modelService, sender, new IMessageParty[] {
			receiver
		});
	}
	
	public IMessageBuilder(IModelService modelService, IMessageParty sender,
		IMessageParty... receiver){
		super(modelService);
		
		object = modelService.create(IMessage.class);
		object.setSender(sender);
		object.setDeleted(false);
		object.setCreateDateTime(LocalDateTime.now());
		for (IMessageParty iMessageParty : receiver) {
			object.addReceiver(iMessageParty);
		}
	}
	
}
