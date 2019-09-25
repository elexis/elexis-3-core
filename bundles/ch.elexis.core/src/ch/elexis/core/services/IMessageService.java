package ch.elexis.core.services;

import org.eclipse.core.runtime.IStatus;

import ch.elexis.core.model.IMessage;

/**
 * Handles transportation of an {@link IMessage}
 */
public interface IMessageService {
	
	/**
	 * Create an {@link IMessage} object for further parameterization
	 * 
	 * @param sender
	 * @param receiver
	 * @return
	 */
	IMessage prepare(String sender, String... receiver);
	
	/**
	 * Send the message
	 * 
	 * @param message
	 * @return
	 */
	IStatus send(IMessage message);
	
}
