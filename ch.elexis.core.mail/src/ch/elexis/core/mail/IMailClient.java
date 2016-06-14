package ch.elexis.core.mail;

import java.util.List;
import java.util.Optional;

public interface IMailClient {
	
	enum ErrorTyp {
			CONNECTION, AUTHENTICATION, CONFIGTYP, ADDRESS
	}
	
	public void saveAccount(MailAccount account);
	
	public void removeAccount(MailAccount account);
	
	public Optional<MailAccount> getAccount(String id);
	
	public List<String> getAccounts();
	
	public Optional<String> getDefaultAccount();
	
	public void setDefaultAccount(String id);
	
	public boolean testAccount(MailAccount account);
	
	public Optional<ErrorTyp> getLastError();
	
	public boolean sendMail(MailAccount account, MailMessage message);
}
