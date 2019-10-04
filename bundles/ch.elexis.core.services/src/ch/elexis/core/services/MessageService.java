package ch.elexis.core.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Status;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import ch.elexis.core.model.message.TransientMessage;
import ch.elexis.core.services.internal.Bundle;
import ch.elexis.core.status.ObjectStatus;

@Component
public class MessageService implements IMessageService {
	
	private Map<String, IMessageTransporter> messageTransporters;
	
	public MessageService(){
		messageTransporters = new HashMap<>();
	}
	
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	public void setMessageTransporter(IMessageTransporter messageTransporter){
		if (!messageTransporters.containsKey(messageTransporter.getUriScheme())) {
			messageTransporters.put(messageTransporter.getUriScheme(), messageTransporter);
		}
	}
	
	public void unsetMessageTransporter(IMessageTransporter messageTransporter){
		if (messageTransporters.containsKey(messageTransporter.getUriScheme())) {
			messageTransporters.remove(messageTransporter.getUriScheme());
		}
	}
	
	@Override
	public TransientMessage prepare(String sender, String receiver){
		return new TransientMessage(sender, receiver);
	}
	
	@Override
	public List<String> getSupportedUriSchemes(){
		return new ArrayList<String>(messageTransporters.keySet());
	}
	
	@Override
	public ObjectStatus send(TransientMessage message){
		
		String receiver = message.getReceiver();
		int indexOf = receiver.indexOf(':');
		if (indexOf <= 0) {
			return new ObjectStatus(Status.ERROR, Bundle.ID,
				"No transporter uri scheme found in receiver [" + receiver + "]", null);
		}
		
		String uriScheme = receiver.substring(0, indexOf);
		IMessageTransporter messageTransporter = null;
		if (uriScheme.equals(INTERNAL_MESSAGE_URI_SCHEME)) {
			messageTransporter = selectInternalSchemeTransporter();
		} else {
			messageTransporter = messageTransporters.get(uriScheme);
		}
		
		if (messageTransporter == null) {
			return new ObjectStatus(Status.ERROR, Bundle.ID,
				"No transporter found for uri scheme [" + uriScheme + "]", null);
		}
		
		return new ObjectStatus(messageTransporter.send(message),
			messageTransporter.getUriScheme());
	}
	
	/**
	 * Select a transporter for an internal message. Currently we prefer the rocketchat transporter
	 * (if available).
	 * 
	 * @return the transporter or <code>null</code> if none available
	 */
	private IMessageTransporter selectInternalSchemeTransporter(){
		IMessageTransporter messageTransporter = messageTransporters.get("rocketchat");
		if (messageTransporter == null) {
			messageTransporter = messageTransporters.get("internaldb");
		}
		return messageTransporter;
	}
	
}
