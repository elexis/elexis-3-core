package ch.elexis.core.mail.model;

/**
 * Identity returned after an interactive OAuth sign-in has been persisted.
 * @param accountId the saved mailbox account
 * @param signedInAs the provider user's address, possibly different from the mailbox address
 */
public record MailOAuthSignIn(String accountId, String signedInAs) {
}
