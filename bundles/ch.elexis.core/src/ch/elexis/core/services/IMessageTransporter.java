package ch.elexis.core.services;

import org.eclipse.core.runtime.IStatus;

import ch.elexis.core.model.IMessage;
import ch.elexis.core.model.message.TransientMessage;

public interface IMessageTransporter extends Comparable<IMessageTransporter> {
	
	/**
	 * @return the default priority of this transporter, higher value is higher priority. If the
	 *         priority is lower than 0, the transporter is not added as a default transporter, and
	 *         may only be addressed via {@link IMessage#getPreferredTransporters()}
	 */
	int getDefaultPriority();
	
	/**
	 * @return a unique identifier
	 */
	String getId();
	
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
	
	@Override
	default int compareTo(IMessageTransporter other){
		return Integer.compare(getDefaultPriority(), other.getDefaultPriority());
	}
}
