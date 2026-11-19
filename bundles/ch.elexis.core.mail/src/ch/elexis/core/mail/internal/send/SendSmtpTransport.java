package ch.elexis.core.mail.internal.send;

import java.io.OutputStream;

import org.eclipse.angus.mail.smtp.SMTPTransport;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.URLName;

public final class SendSmtpTransport extends SMTPTransport {
	private boolean dataStarted;

	public SendSmtpTransport(Session session, URLName url, boolean ssl) {
		super(session, url, "smtp", ssl);
	}

	@Override
	protected OutputStream data() throws MessagingException {
		OutputStream output = super.data();
		dataStarted = true;
		return output;
	}

	boolean dataStarted() {
		return dataStarted;
	}
}
