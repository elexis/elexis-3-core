package ch.elexis.core.services;

import org.eclipse.core.runtime.IStatus;

import ch.elexis.core.model.IMessage;
import ch.elexis.core.model.IMessageParty;
import ch.elexis.core.model.message.MessageParty;

/**
 * Handles transportation of an {@link IMessage} between {@link IMessageParty} participants.
 */
public interface IMessageService {
	
	/**
	 * Create an {@link IMessage} object for further parameterization
	 * 
	 * @param sender
	 * @param receiver
	 * @return
	 */
	IMessage prepare(MessageParty sender, MessageParty... receiver);
	
	/**
	 * Send the message
	 * 
	 * @param message
	 * @return
	 */
	IStatus send(IMessage message);
	
}
