package ch.elexis.core.services;

import org.eclipse.core.runtime.IStatus;

import ch.elexis.core.model.IMessage;
import ch.elexis.core.model.message.TransientMessage;

public interface IMessageTransporter {
	
	/**
	 * @return the uri scheme denominator
	 */
	String getUriScheme();
	
	/**
	 * Try to send an {@link IMessage}.
	 * 
	 * @param message
	 *            to send
	 * @return {@link IStatus#OK} if the message could be sent, {@link IStatus#CANCEL} if message
	 *         not supported by this transporter (e.g. sender is not valid), or
	 *         {@link IStatus#ERROR} if any other failure
	 */
	IStatus send(TransientMessage message);
	
	/**
	 * 
	 * @return whether a message transported with this transporter "leaves the site boundary". E.g.
	 *         an SMS transporter
	 */
	boolean isExternal();
	
}
