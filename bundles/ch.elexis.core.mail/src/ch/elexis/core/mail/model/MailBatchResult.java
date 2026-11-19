package ch.elexis.core.mail.model;

/**
 * Counts the affected and no longer existing identities of one batch.
 * @param changed the number of affected messages
 * @param missing the number of identities that no longer exist
 */
public record MailBatchResult(int changed, int missing) {
}
