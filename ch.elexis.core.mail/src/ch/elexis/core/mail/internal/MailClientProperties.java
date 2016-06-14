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
			props.put("mail.transport.protocol", "imaps");
			
			props.put("mail.imaps.host", account.getHost());
			props.put("mail.imaps.port", account.getPort());
		}
		props.put("mail.debug", "true");
		
		return props;
	}
}
