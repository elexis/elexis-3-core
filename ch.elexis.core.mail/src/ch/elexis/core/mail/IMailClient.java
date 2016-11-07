package ch.elexis.core.mail;

import java.util.List;
import java.util.Optional;

/**
 * Interface to the email functionality of Elexis. Configure and access {@link MailAccount}
 * instances, and use those to send email.
 * 
 * @author thomas
 *
 */
public interface IMailClient {
	
	/**
	 * Definition of possible errors. Accessible via {@link IMailClient#getLastError()}.
	 * 
	 * @author thomas
	 *
	 */
	enum ErrorTyp {
			CONNECTION, AUTHENTICATION, CONFIGTYP, ADDRESS
	}
	
	/**
	 * Save a {@link MailAccount} to the global config.
	 * 
	 * @param account
	 */
	public void saveAccount(MailAccount account);
	
	/**
	 * Remove a {@link MailAccount} from the global config.
	 * 
	 * @param account
	 */
	public void removeAccount(MailAccount account);
	
	/**
	 * Get a specific {@link MailAccount} via its id.
	 * 
	 * @param id
	 * @return
	 */
	public Optional<MailAccount> getAccount(String id);
	
	/**
	 * Get all configured {@link MailAccount} instances.
	 * 
	 * @return
	 */
	public List<String> getAccounts();
	
	/**
	 * Do a basic connection test if the mail service specified by the {@link MailAccount} is
	 * available.
	 * 
	 * @param account
	 * @return
	 */
	public boolean testAccount(MailAccount account);
	
	/**
	 * Get the {@link ErrorTyp} that occurred during the last operation.
	 * 
	 * @return
	 */
	public Optional<ErrorTyp> getLastError();
	
	/**
	 * Send the {@link MailMessage} using the {@link MailAccount}.
	 * 
	 * @param account
	 * @param message
	 * @return
	 */
	public boolean sendMail(MailAccount account, MailMessage message);
}
