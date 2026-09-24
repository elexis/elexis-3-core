package ch.elexis.core.mail.internal;

import java.util.Properties;

import ch.elexis.core.mail.MailAccount;
import ch.elexis.core.mail.MailAccount.TYPE;

public class MailClientProperties {

	private MailAccount account;

	public MailClientProperties(MailAccount account) {
		this.account = account;
	}

	public Properties getProperties() {
		Properties props = new Properties();

		if (account.getType() == TYPE.SMTP) {
			// SMTP properties
			// https://jakarta.ee/specifications/mail/1.6/apidocs/?com/sun/mail/smtp/package-summary.html
			props.put("mail.transport.protocol", "smtp");

			if (account.getUsername() != null && account.getPassword() != null) {
				props.put("mail.user", account.getUsername());
				props.put("mail.smtp.auth", "true");
			}

			props.put("mail.smtp.host", account.getHost());
			props.put("mail.smtp.port", account.getPort());
			props.put("mail.smtp.starttls.enable", Boolean.toString(account.isStarttls()));

			props.put("mail.smtp.connectiontimeout", 30 * 1000);
			props.put("mail.smtp.timeout", 240 * 1000);
			props.put("mail.smtp.writetimeout", 240 * 1000);

		} else if (account.getType() == TYPE.IMAP) {
			// IMAP properties
			// https://javaee.github.io/javamail/docs/api/com/sun/mail/imap/package-summary.html
			props.put("mail.store.protocol", "imap");
			props.put("mail.imap.host", account.getHost());
			props.put("mail.imap.port", account.getPort());
			props.put("mail.smtp.starttls.enable", Boolean.toString(account.isStarttls()));

			if (account.getUsername() != null && account.getPassword() != null) {
				props.put("mail.imap.user", account.getUsername());
			}

			props.put("mail.imap.connectiontimeout", 30 * 1000);
			props.put("mail.imap.timeout", 240 * 1000);
			props.put("mail.imap.writetimeout", 240 * 1000);

		} else if (account.getType() == TYPE.IMAPS) {
			props.put("mail.store.protocol", "imaps");
			props.put("mail.imaps.host", account.getHost());
			props.put("mail.imaps.port", account.getPort());

			props.put("mail.imaps.connectiontimeout", 30 * 1000);
			props.put("mail.imaps.timeout", 240 * 1000);
			props.put("mail.imaps.writetimeout", 240 * 1000);

			if (account.getUsername() != null && account.getPassword() != null) {
				props.put("mail.imap.user", account.getUsername());
			}

		}
//		props.put("mail.debug", "true");

		return props;
	}
}
