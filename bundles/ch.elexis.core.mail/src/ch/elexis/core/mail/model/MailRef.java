package ch.elexis.core.mail.model;

/**
 * Immutable reference to one message inside one folder of one account.
 *
 * @param accountId
 *            the id of the account holding the message
 * @param folder
 *            the full name of the folder
 * @param uidValidity
 *            the UIDVALIDITY of the folder when the reference was created
 * @param uid
 *            the IMAP UID of the message
 */
public record MailRef(String accountId, String folder, long uidValidity, long uid) {
}
