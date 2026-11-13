package ch.elexis.core.mail.internal.connection;

import java.util.Properties;
import java.util.function.Predicate;

import org.eclipse.angus.mail.iap.CommandFailedException;
import org.eclipse.angus.mail.iap.ProtocolException;
import org.eclipse.angus.mail.imap.protocol.IMAPProtocol;
import org.eclipse.angus.mail.imap.IMAPSSLStore;
import org.eclipse.angus.mail.imap.IMAPStore;

import ch.elexis.core.mail.model.AuthMode;
import ch.elexis.core.mail.model.MailSecurity;
import ch.elexis.core.mail.model.MailboxAccount;
import ch.elexis.core.mail.model.ServerEndpoint;
import jakarta.mail.Session;
import jakarta.mail.URLName;

/** Builds isolated IMAP sessions and creates Angus stores without provider lookup. */
public final class MailSessionFactory {
	private final Predicate<String> legacy;

	/** Creates sessions that require encrypted transport outside IPv4 loopback. */
	public MailSessionFactory() {
		this(id -> false);
	}

	/**
	 * Creates sessions with an explicit inherited-account security policy.
	 * @param legacy checks whether the account may retain unencrypted transport
	 */
	public MailSessionFactory(Predicate<String> legacy) {
		this.legacy = legacy;
	}

	/**
	 * Builds a bounded SMTP session with mandatory STARTTLS when configured.
	 * @param account the account configuration
	 * @return a new SMTP session without credentials
	 * @throws IllegalArgumentException if unencrypted access does not target IPv4 loopback
	 */
	public Session createSmtpSession(MailboxAccount account) {
		ServerEndpoint endpoint = account.smtp();
		if (endpoint.security() == MailSecurity.NONE && !"127.0.0.1".equals(endpoint.host()) && !legacy.test(account.id())) {
			throw new IllegalArgumentException("Unencrypted mail requires IPv4 loopback");
		}
		Properties properties = new Properties();
		String prefix = "mail.smtp.";
		properties.setProperty(prefix + "connectiontimeout", "15000");
		properties.setProperty(prefix + "timeout", "60000");
		properties.setProperty(prefix + "writetimeout", "30000");
		properties.setProperty(prefix + "ssl.checkserveridentity", "true");
		properties.setProperty(prefix + "ssl.enable", Boolean.toString(endpoint.security() == MailSecurity.SSL));
		boolean startTls = endpoint.security() == MailSecurity.STARTTLS;
		properties.setProperty(prefix + "starttls.enable", Boolean.toString(startTls));
		properties.setProperty(prefix + "starttls.required", Boolean.toString(startTls));
		properties.setProperty(prefix + "auth", "true");
		properties.setProperty(prefix + "sendpartial", "false");
		properties.setProperty(prefix + "quitwait", "false");
		if (account.authMode() == AuthMode.OAUTH2) {
			properties.setProperty(prefix + "auth.mechanisms", "XOAUTH2");
		}
		properties.setProperty("mail.mime.decodefilename", "true");
		return Session.getInstance(properties);
	}

	/**
	 * Builds the bounded, non-marking IMAP session for an account.
	 * @param account the account configuration
	 * @return a new session without credentials
	 * @throws IllegalArgumentException if unencrypted access does not target IPv4 loopback
	 */
	public Session createSession(MailboxAccount account) {
		ServerEndpoint endpoint = account.imap();
		if (endpoint.security() == MailSecurity.NONE && !"127.0.0.1".equals(endpoint.host()) && !legacy.test(account.id())) {
			throw new IllegalArgumentException("Unencrypted mail requires IPv4 loopback");
		}
		Properties properties = new Properties();
		for (String protocol : new String[] { "imap", "imaps" }) {
			String prefix = "mail." + protocol + ".";
			properties.setProperty(prefix + "connectiontimeout", "15000");
			properties.setProperty(prefix + "timeout", "60000");
			properties.setProperty(prefix + "writetimeout", "30000");
			properties.setProperty(prefix + "partialfetch", "true");
			properties.setProperty(prefix + "fetchsize", "262144");
			properties.setProperty(prefix + "peek", "true");
			properties.setProperty(prefix + "throwsearchexception", "true");
			properties.setProperty(prefix + "ssl.checkserveridentity", "true");
			properties.setProperty(prefix + "ssl.enable", Boolean.toString(endpoint.security() == MailSecurity.SSL));
			boolean startTls = endpoint.security() == MailSecurity.STARTTLS;
			properties.setProperty(prefix + "starttls.enable", Boolean.toString(startTls));
			properties.setProperty(prefix + "starttls.required", Boolean.toString(startTls));
			if (account.authMode() == AuthMode.OAUTH2) {
				properties.setProperty(prefix + "auth.mechanisms", "XOAUTH2");
			}
		}
		properties.setProperty("mail.mime.address.strict", "false");
		properties.setProperty("mail.mime.decodefilename", "true");
		return Session.getInstance(properties);
	}

	/**
	 * Constructs an unconnected store directly, without an OSGi context class loader lookup.
	 * @param account the account configuration
	 * @return an unconnected Angus store
	 * @throws IllegalArgumentException if transport security is invalid
	 */
	public IMAPStore createStore(MailboxAccount account) {
		return createStore(account, false);
	}

	IMAPStore createLaneStore(MailboxAccount account) {
		return createStore(account, true);
	}

	private IMAPStore createStore(MailboxAccount account, boolean lane) {
		Session session = createSession(account);
		if (lane) {
			for (String protocol : new String[] { "imap", "imaps" }) {
				session.getProperties().setProperty("mail." + protocol + ".connectionpoolsize", "2");
				session.getProperties().setProperty("mail." + protocol + ".separatestoreconnection", "true");
			}
		}
		ServerEndpoint endpoint = account.imap();
		boolean ssl = endpoint.security() == MailSecurity.SSL;
		URLName url = new URLName(ssl ? "imaps" : "imap", endpoint.host(), endpoint.port(), null,
				account.username(), null);
		if (account.authMode() == AuthMode.OAUTH2) {
			return new OAuthStore(session, url, ssl);
		}
		return ssl ? new IMAPSSLStore(session, url) : new IMAPStore(session, url);
	}

	private static final class OAuthStore extends IMAPStore {
		private OAuthStore(Session session, URLName url, boolean ssl) {
			super(session, url, ssl ? "imaps" : "imap", ssl);
		}

		@Override
		protected void preLogin(IMAPProtocol protocol) throws ProtocolException {
			if (!protocol.hasCapability("AUTH=XOAUTH2")) {
				throw new CommandFailedException("Mail server does not support XOAUTH2");
			}
		}
	}
}
