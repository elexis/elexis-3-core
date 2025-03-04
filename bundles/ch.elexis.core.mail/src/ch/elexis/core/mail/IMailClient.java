package ch.elexis.core.mail;

import java.util.List;
import java.util.Optional;

import org.eclipse.angus.mail.imap.IMAPMessage;

import ch.elexis.core.jdt.Nullable;
import jakarta.mail.MessagingException;

/**
 * Interface to the email functionality of Elexis. Configure and access
 * {@link MailAccount} instances, and use those to send or receive email.
 *
 * @author thomas
 *
 */
public interface IMailClient {

	/**
	 * Definition of possible errors. Accessible via
	 * {@link IMailClient#getLastError()}.
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
	 * Save a {@link MailAccount} to the local config.
	 *
	 * @param account
	 */
	public void saveAccountLocal(MailAccount account);

	/**
	 * Remove a {@link MailAccount} from the global config.
	 *
	 * @param account
	 */
	public void removeAccount(MailAccount account);

	/**
	 * Remove a {@link MailAccount} from the local config.
	 *
	 * @param account
	 */
	public void removeAccountLocal(MailAccount account);

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
	 * Get all configured {@link MailAccount} instances.
	 *
	 * @return
	 */
	public List<String> getAccountsLocal();

	/**
	 * Do a basic connection test if the mail service specified by the
	 * {@link MailAccount} is available.
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
	 * Retrieve all messages from a {@link MailAccount.TYPE#IMAP}. Creates a
	 * self-sustainable copy of the message.
	 *
	 * @param account            the IMAP account to fetch the messages from
	 * @param folderPath         a relative folder path, defaults no
	 *                           <code>INBOX</code> if <code>null</code>
	 * @param flag               flag fetched messages
	 * @param onlyFetchUnflagged only fetch messages that are not flagged
	 * @throws MessagingException if there is an error opening the connection
	 * @return
	 */
	public List<IMAPMailMessage> getMessages(MailAccount account, String folderPath, boolean flag,
			boolean onlyFetchUnflagged) throws MessagingException;

	/**
	 * Try to move a message from a given source folder to a target folder.
	 *
	 * @param account      the IMAP account to fetch the messages from
	 * @param message      to move
	 * @param sourceFolder a relative folder path, defaults no <code>INBOX</code> if
	 *                     <code>null</code>
	 * @param targetFolder not <code>null</code>
	 * @param removeFlag   remove the flag that may have been set
	 * @throws MessagingException if there is an error while moving the
	 *                            {@link IMAPMessage}
	 */
	public void moveMessage(MailAccount account, IMAPMailMessage message, @Nullable String sourceFolder,
			String targetFolder, boolean removeFlag) throws MessagingException;

}
