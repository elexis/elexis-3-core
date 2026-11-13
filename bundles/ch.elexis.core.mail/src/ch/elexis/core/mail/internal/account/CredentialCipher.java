package ch.elexis.core.mail.internal.account;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/** Encrypts credentials with a salt-specific, cached AES-256-GCM key. */
public final class CredentialCipher {
	private static final String BUNDLE_SECRET = "Medelexis-Mail-Client-Credential-v1";
	private static final int IV_LENGTH = 12;
	private static final int SALT_LENGTH = 32;
	private final SecureRandom random = new SecureRandom();
	private final Supplier<String> readSalt;
	private final Consumer<String> writeSalt;
	private String cachedSalt;
	private SecretKey cachedKey;

	/**
	 * Creates a cipher with an explicit installation salt and no configuration access.
	 * @param salt the 32-byte installation salt
	 * @throws GeneralSecurityException if the salt is invalid or key derivation fails
	 */
	public CredentialCipher(byte[] salt) throws GeneralSecurityException {
		if (salt.length != SALT_LENGTH) {
			throw new GeneralSecurityException("Invalid credential salt");
		}
		String encoded = Base64.getEncoder().encodeToString(salt);
		readSalt = () -> encoded;
		writeSalt = value -> {
			throw new IllegalStateException("Explicit credential salt cannot be replaced");
		};
		key();
	}

	/**
	 * Creates a lazy cipher backed by configuration functions.
	 * @param readSalt reads the installation salt, or null if absent
	 * @param writeSalt persists a newly generated salt without a trace entry
	 */
	public CredentialCipher(Supplier<String> readSalt, Consumer<String> writeSalt) {
		this.readSalt = readSalt;
		this.writeSalt = writeSalt;
	}

	/**
	 * Encrypts a password with a fresh 12-byte IV.
	 * @param value the plaintext; ownership stays with the caller
	 * @return the versioned, Base64-encoded authenticated ciphertext
	 * @throws GeneralSecurityException if encryption or key derivation fails
	 */
	public String encrypt(char[] value) throws GeneralSecurityException {
		byte[] iv = new byte[IV_LENGTH];
		random.nextBytes(iv);
		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, key(), new GCMParameterSpec(128, iv));
		ByteBuffer encoded = StandardCharsets.UTF_8.encode(CharBuffer.wrap(value));
		byte[] plaintext = new byte[encoded.remaining()];
		encoded.get(plaintext);
		try {
			byte[] encrypted = cipher.doFinal(plaintext);
			byte[] payload = ByteBuffer.allocate(iv.length + encrypted.length).put(iv).put(encrypted).array();
			return "v1:" + Base64.getEncoder().encodeToString(payload);
		} finally {
			Arrays.fill(plaintext, (byte) 0);
			if (encoded.hasArray()) {
				Arrays.fill(encoded.array(), (byte) 0);
			}
		}
	}

	/**
	 * Decrypts and authenticates a stored credential.
	 * @param value the versioned ciphertext
	 * @return the plaintext; the caller must clear the returned array
	 * @throws GeneralSecurityException for an unknown version, malformed value or authentication failure
	 */
	public char[] decrypt(String value) throws GeneralSecurityException {
		if (value == null || !value.startsWith("v1:")) {
			throw new GeneralSecurityException("Unsupported credential format");
		}
		byte[] payload;
		try {
			payload = Base64.getDecoder().decode(value.substring(3));
		} catch (IllegalArgumentException e) {
			throw new GeneralSecurityException("Invalid credential encoding", e);
		}
		if (payload.length < IV_LENGTH + 16) {
			throw new GeneralSecurityException("Invalid credential payload");
		}
		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
		cipher.init(Cipher.DECRYPT_MODE, key(),
				new GCMParameterSpec(128, Arrays.copyOf(payload, IV_LENGTH)));
		byte[] plaintext = cipher.doFinal(payload, IV_LENGTH, payload.length - IV_LENGTH);
		try {
			CharBuffer decoded = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(plaintext));
			char[] result = new char[decoded.remaining()];
			decoded.get(result);
			if (decoded.hasArray()) {
				Arrays.fill(decoded.array(), '\0');
			}
			return result;
		} finally {
			Arrays.fill(plaintext, (byte) 0);
		}
	}

	private synchronized SecretKey key() throws GeneralSecurityException {
		String salt = readSalt.get();
		if (salt == null || salt.isEmpty()) {
			byte[] generated = new byte[SALT_LENGTH];
			random.nextBytes(generated);
			salt = Base64.getEncoder().encodeToString(generated);
			writeSalt.accept(salt);
			salt = readSalt.get();
			if (salt == null || salt.isEmpty()) {
				throw new GeneralSecurityException("Credential salt was not persisted");
			}
		}
		if (cachedKey != null && Objects.equals(cachedSalt, salt)) {
			return cachedKey;
		}
		byte[] decoded;
		try {
			decoded = Base64.getDecoder().decode(salt);
		} catch (IllegalArgumentException e) {
			throw new GeneralSecurityException("Invalid credential salt", e);
		}
		if (decoded.length != SALT_LENGTH) {
			throw new GeneralSecurityException("Invalid credential salt");
		}
		PBEKeySpec spec = new PBEKeySpec(BUNDLE_SECRET.toCharArray(), decoded, 210000, 256);
		byte[] derived = null;
		try {
			derived = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
			cachedKey = new SecretKeySpec(derived, "AES");
			cachedSalt = salt;
			return cachedKey;
		} finally {
			spec.clearPassword();
			if (derived != null) {
				Arrays.fill(derived, (byte) 0);
			}
		}
	}
}
