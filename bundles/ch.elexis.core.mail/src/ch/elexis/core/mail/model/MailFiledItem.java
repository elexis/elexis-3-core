package ch.elexis.core.mail.model;

/**
 * Outcome for one requested item.
 * @param partPath the MIME part path, null for the complete message
 * @param status the filing outcome
 * @param documentId the existing, new or surviving document id, null when no document was filed
 */
public record MailFiledItem(String partPath, MailFilingStatus status, String documentId) {
}
