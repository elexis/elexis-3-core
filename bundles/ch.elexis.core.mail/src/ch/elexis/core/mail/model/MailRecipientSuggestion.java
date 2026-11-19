package ch.elexis.core.mail.model;

/**
 * A proposed recipient while typing an address.
 *
 * @param name
 *            the display name, may be empty
 * @param address
 *            the mail address
 * @param hint
 *            a distinguishing detail, for example the birth date of a patient, may be <code>null</code>
 * @param recent
 *            <code>true</code> if the user wrote to this address recently
 */
public record MailRecipientSuggestion(String name, String address, String hint, boolean recent) {
}
