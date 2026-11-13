package ch.elexis.core.mail.service;

import java.util.concurrent.CompletableFuture;

import ch.elexis.core.mail.model.MailFilingResult;
import ch.elexis.core.mail.model.MailSendFiling;
import ch.elexis.core.model.IUser;
import jakarta.mail.internet.MimeMessage;

/**
 * Optional extension that files a message after it was sent successfully.
 */
public interface IMailSentFiling {

	/**
	 * Copies a just-sent message before returning and files it on the archive worker.
	 * @param user the active requesting user
	 * @param accountId the visible sending account
	 * @param message the sent message, including its still-readable attachments
	 * @param filing the captured patient and note intent
	 * @return completion with the filing outcome or an authorization, patient or copy failure
	 */
	CompletableFuture<MailFilingResult> fileSent(IUser user, String accountId, MimeMessage message,
			MailSendFiling filing);
}
