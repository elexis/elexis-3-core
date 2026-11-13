package ch.elexis.core.mail.model;

/** Trash identity and purge capability.
 * @param uidValidity current folder identity
 * @param uidNext next assigned UID
 * @param uidPlus whether selective expunge is supported
 */
public record MailTrashStatus(long uidValidity, long uidNext, boolean uidPlus) {
}
