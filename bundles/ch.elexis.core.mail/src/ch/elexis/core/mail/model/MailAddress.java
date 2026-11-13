package ch.elexis.core.mail.model;

/**
 * A mail address with its optional personal name.
 *
 * @param name
 *            the personal name, may be empty
 * @param address
 *            the address
 */
public record MailAddress(String name, String address) {
}
