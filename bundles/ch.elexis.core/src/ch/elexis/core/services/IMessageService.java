package ch.elexis.core.services;

import java.util.List;

import org.eclipse.core.runtime.IStatus;

import ch.elexis.core.model.IMessage;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.message.TransientMessage;

/**
 * Handles transportation of an {@link IMessage}
 */
public interface IMessageService {
	
	/**
	 * @return the available message transporters
	 */
	List<IMessageTransporter> getAvailableTransporters();
	
	/**
	 * Create an {@link IMessage} object for further parameterization
	 * 
	 * @param sender
	 * @param receiver
	 * @return
	 */
	TransientMessage prepare(String sender, String... receiver);
	
	/**
	 * Convenience method for {@link #prepare(String, String...)}
	 * 
	 * @param sender
	 * @param receiver
	 * @return
	 */
	TransientMessage prepare(IUser sender, String... receiver);
	
	/**
	 * Try to send the message.
	 * 
	 * @param message
	 * @return
	 */
	IStatus send(TransientMessage message);
	
}
