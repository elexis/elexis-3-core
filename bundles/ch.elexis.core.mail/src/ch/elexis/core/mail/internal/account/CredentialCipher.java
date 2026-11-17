package ch.elexis.core.mail.internal.account;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

/** Encodes stored credentials as Base64, like the legacy mail accounts. */
public final class CredentialCipher {

	/**
	 * Encodes a credential.
	 * @param value the plaintext; ownership stays with the caller
	 * @return the Base64-encoded value
	 */
	public String encrypt(char[] value) {
		return Base64.getEncoder().encodeToString(new String(value).getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Decodes a stored credential.
	 * @param value the Base64-encoded value
	 * @return the plaintext
	 * @throws GeneralSecurityException if the value is not valid Base64
	 */
	public char[] decrypt(String value) throws GeneralSecurityException {
		try {
			return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8).toCharArray();
		} catch (IllegalArgumentException e) {
			throw new GeneralSecurityException("Invalid credential encoding", e);
		}
	}
}
