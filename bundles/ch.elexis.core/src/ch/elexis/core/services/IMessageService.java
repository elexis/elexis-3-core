package ch.elexis.core.services;

import java.util.List;

import ch.elexis.core.model.message.TransientMessage;
import ch.elexis.core.status.ObjectStatus;

/**
 * Transport a message from a sender to a receiver
 */
public interface IMessageService {

	public static final String INTERNAL_MESSAGE_URI_SCHEME = "internal";

	/**
	 * @return a list of all supported URI schemes
	 */
	List<String> getSupportedUriSchemes();

	/**
	 *
	 * @param sender       a string which may or may be not resolvable to a user or
	 *                     supported scheme
	 * @param recipientUri a recipient URI scheme and the user.<br>
	 *                     For internal communication either explicitly select the
	 *                     transporter e.g. <code>internaldb:user</code>,
	 *                     <code>matrix:user</code> or use the implicit
	 *                     {@link #INTERNAL_MESSAGE_URI_SCHEME} to leave the choice
	 *                     to the system.<br>
	 *                     For external communication use
	 *                     <code>mailto:user@bla.com</code> or
	 *                     <code>sms:+4133423</code> (if available). A message has
	 *                     to be explicitly marked with
	 *                     {@link TransientMessage#setAlllowExternal(boolean)} to
	 *                     use an external transporter.
	 * @return
	 */
	TransientMessage prepare(String sender, String recipientUri);

	/**
	 * Try to send the message.
	 *
	 * @param message
	 * @return if the message was sent successfully, an optional message id as
	 *         {@link ObjectStatus#getMessage()} and the explicit transporter URI
	 *         scheme used in {@link ObjectStatus#getObject()}
	 */
	ObjectStatus send(TransientMessage message);

}
