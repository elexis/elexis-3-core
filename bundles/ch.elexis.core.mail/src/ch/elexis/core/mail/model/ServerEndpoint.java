package ch.elexis.core.mail.model;

/**
 * Address and transport security of an IMAP or SMTP server.
 *
 * @param host
 *            the host name, <code>NONE</code> security is only allowed for <code>127.0.0.1</code>
 * @param port
 *            the port
 * @param security
 *            the transport security
 */
public record ServerEndpoint(String host, int port, MailSecurity security) {
}
