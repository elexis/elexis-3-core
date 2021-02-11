package ch.elexis.core.mail.internal;

import java.util.Properties;

import ch.elexis.core.mail.MailAccount;
import ch.elexis.core.mail.MailAccount.TYPE;

public class MailClientProperties {
	
	private MailAccount account;
	
	public MailClientProperties(MailAccount account){
		this.account = account;
	}
	
	public Properties getProperties(){
		Properties props = new Properties();
		
		if (account.getType() == TYPE.SMTP) {
			props.put("mail.transport.protocol", "smtp");
			
			if (account.getUsername() != null && account.getPassword() != null) {
				props.put("mail.user", account.getUsername());
				props.put("mail.smtp.auth", "true");
			}
			
			props.put("mail.smtp.host", account.getHost());
			props.put("mail.smtp.port", account.getPort());
			props.put("mail.smtp.starttls.enable", Boolean.toString(account.isStarttls()));
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

		} else if (account.getType() == TYPE.IMAPS) {
			props.put("mail.store.protocol", "imaps");
			props.put("mail.imaps.host", account.getHost());
			props.put("mail.imaps.port", account.getPort());
			
			if (account.getUsername() != null && account.getPassword() != null) {
				props.put("mail.imap.user", account.getUsername());
			}

		}
//		props.put("mail.debug", "true");
		
		return props;
	}
}
