package ch.elexis.core.mail.internal.connection;

import org.eclipse.angus.mail.iap.Response;
import org.eclipse.angus.mail.imap.IMAPFolder;
import org.eclipse.angus.mail.imap.IMAPStore;
import org.eclipse.angus.mail.imap.protocol.IMAPResponse;

import jakarta.mail.MessagingException;
import jakarta.mail.Message;

/** Tracks the live server count separately from Angus message slots retained after EXPUNGE. */
public final class MailReadFolder extends IMAPFolder {
	private int expunged;
	private boolean selected;

	MailReadFolder(String name, IMAPStore store) {
		super(name, '\uffff', store, null);
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void open(int mode) throws MessagingException {
		expunged = 0;
		super.open(mode);
		selected = true;
	}

	/** {@inheritDoc} */
	@Override
	public synchronized boolean isOpen() {
		return selected;
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void close(boolean expunge) throws MessagingException {
		try {
			super.close(expunge);
		} finally {
			selected = false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void forceClose() throws MessagingException {
		try {
			super.forceClose();
		} finally {
			selected = false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public void handleResponse(Response response) {
		if (response.isBYE()) {
			selected = false;
		}
		if (response instanceof IMAPResponse imap && imap.keyEquals("EXPUNGE")) {
			expunged++;
		}
		super.handleResponse(response);
	}

	/**
	 * Returns the message count minus the number of observed expunges.
	 * @return the live server message count
	 * @throws MessagingException if the message count cannot be read
	 */
	public int liveCount() throws MessagingException {
		return getMessageCount() - expunged;
	}

	boolean hasExpunged() {
		return expunged > 0;
	}

	/**
	 * Resolves a live server sequence number to its retained message number.
	 * @param sequence the server sequence number
	 * @return the retained message number
	 * @throws MessagingException if the sequence no longer exists or cannot be resolved
	 */
	public int messageNumber(int sequence) throws MessagingException {
		Message message = getMessageBySeqNumber(sequence);
		if (message == null) {
			throw new MessagingException("Mail sequence no longer exists");
		}
		return message.getMessageNumber();
	}
}
