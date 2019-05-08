package ch.elexis.core.model.builder;

import java.time.LocalDateTime;

import ch.elexis.core.model.IMessage;
import ch.elexis.core.model.IMessageParty;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.message.MessageParty;
import ch.elexis.core.services.IModelService;

public class IMessageBuilder extends AbstractBuilder<IMessage>{

	public IMessageBuilder(IModelService modelService, IUser sender, IUser receiver) {
		this(modelService, new MessageParty(sender), new MessageParty(receiver));
	}
	
	public IMessageBuilder(IModelService modelService, IMessageParty sender, IUser receiver) {
		this(modelService, sender, new MessageParty(receiver));
	}
	
	public IMessageBuilder(IModelService modelService, IMessageParty sender, IMessageParty receiver){
		super(modelService);
		
		object.setSender(sender);
		object.addReceiver(receiver);
		object.setDeleted(false);
		object.setCreateDateTime(LocalDateTime.now());
	}
	
}
