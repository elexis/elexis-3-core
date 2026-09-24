package ch.elexis.core.services;

import org.eclipse.core.runtime.IStatus;

import ch.elexis.core.model.IMessage;
import ch.elexis.core.model.message.TransientMessage;
import ch.elexis.core.status.ObjectStatus;

public interface IMessageTransporter {

	/**
	 * @return the uri scheme denominator
	 */
	String getUriScheme();

	/**
	 * Try to send an {@link IMessage}.
	 *
	 * @param message to send
	 * @return {@link ObjectStatus#OK} with the message ID as
	 *         {@link IStatus#getMessage()} if the message could be sent;
	 *         {@link IStatus#CANCEL} if the message not supported by this
	 *         transporter (e.g. sender is not valid), or {@link IStatus#ERROR} with
	 *         reason message on failure
	 */
	IStatus send(TransientMessage message);

	/**
	 *
	 * @return whether a message transported with this transporter "leaves the site
	 *         boundary". E.g. an SMS transporter
	 */
	boolean isExternal();

}
