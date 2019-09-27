package ch.elexis.core.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IUser;
import ch.elexis.core.model.message.TransientMessage;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.status.StatusUtil;

@Component
public class MessageService implements IMessageService {
	
	@Reference
	private IContextService contextService;
	
	/**
	 * all transporters available
	 */
	private List<IMessageTransporter> messageTransporters;
	/**
	 * transporters considered for default message transportation
	 */
	private List<IMessageTransporter> defaultTransporters;
	
	public MessageService(){
		messageTransporters = new ArrayList<>();
		defaultTransporters = new ArrayList<>();
	}
	
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	public void setMessageTransporter(IMessageTransporter messageTransporter){
		if (!messageTransporters.contains(messageTransporter)) {
			messageTransporters.add(messageTransporter);
			if (messageTransporter.getDefaultPriority() >= 0) {
				defaultTransporters.add(messageTransporter);
			}
			Collections.sort(defaultTransporters);
		}
	}
	
	public void unsetMessageTransporter(IMessageTransporter messageTransporter){
		if (messageTransporters.contains(messageTransporter)) {
			messageTransporters.remove(messageTransporter);
			if (messageTransporter.getDefaultPriority() >= 0) {
				defaultTransporters.remove(messageTransporter);
			}
			Collections.sort(defaultTransporters);
		}
	}
	
	@Override
	public TransientMessage prepare(String sender, String... receiver){
		boolean senderIsUser = CoreModelServiceHolder.get().load(sender, IUser.class).isPresent();
		return new TransientMessage(sender, senderIsUser, receiver);
	}
	
	@Override
	public TransientMessage prepare(IUser sender, String... receiver){
		return new TransientMessage(sender.getId(), true, receiver);
	}
	
	@Override
	public List<IMessageTransporter> getAvailableTransporters(){
		return new ArrayList<IMessageTransporter>(messageTransporters);
	}
	
	@Override
	public IStatus send(TransientMessage message){
		List<IMessageTransporter> consideredTransporters;
		List<String> preferredTransporters = message.getPreferredTransporters();
		if (preferredTransporters.isEmpty()) {
			consideredTransporters = defaultTransporters;
		} else {
			consideredTransporters = new ArrayList<>();
			// TODO populate
		}
		
		IStatus status = null;
		for (IMessageTransporter messageTransporter : consideredTransporters) {
			status = messageTransporter.send(message);
			if (status.isOK()) {
				return status;
			} else {
				StatusUtil.logStatus(LoggerFactory.getLogger(getClass()), status);
			}
			// if CANCEL or ERROR continue
		}
		
		if (status == null) {
			status = new Status(Status.ERROR, "", "No message transporter found");
		}
		
		return status;
	}
	
}
