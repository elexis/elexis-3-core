package ch.elexis.core.mail;

import java.util.List;
import java.util.Optional;

import javax.mail.MessagingException;

import com.sun.mail.imap.IMAPMessage;

/**
 * Interface to the email functionality of Elexis. Configure and access {@link MailAccount}
 * instances, and use those to send or receive email.
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
	
	/**
	 * Retrieve all messages from a {@link MailAccount.TYPE#IMAP}. Opens a connection to the IMAP
	 * server. Be sure to {@link #closeStore(MailAccount)} after you're done.
	 * 
	 * @param account
	 *            the IMAP account to fetch the messages from
	 * @param folderPath
	 *            a relative folder path, defaults no <code>INBOX</code> if <code>null</code>
	 * @throws MessagingException
	 *             if there is an error opening the connection
	 * @return
	 */
	public List<IMAPMailMessage> getMessages(MailAccount account, String folderPath)
		throws MessagingException;
	
	/**
	 * Move a message from a given source folder to a target folder. Uses the connection to the IMAP
	 * server that was opened during {@link #getMessages(MailAccount, String)}.
	 * 
	 * @param message
	 *            to move
	 * @param targetFolder
	 * @throws MessagingException
	 *             if there is an error while moving the {@link IMAPMessage}
	 */
	public void moveMessage(IMAPMailMessage message, String targetFolder) throws MessagingException;
	
	/**
	 * Close the connection to the IMAP server.
	 * 
	 * @param account
	 */
	public void closeStore(MailAccount account);
	
}
