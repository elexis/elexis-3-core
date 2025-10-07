package ch.elexis.core.services.oauth2;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * @see https://www.appsdeveloperblog.com/pkce-code-verifier-and-code-challenge-in-java/
 */
public class PKCEUtil {

	String generateCodeVerifier() throws UnsupportedEncodingException {
		SecureRandom secureRandom = new SecureRandom();
		byte[] codeVerifier = new byte[32];
		secureRandom.nextBytes(codeVerifier);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
	}

	String generateCodeChallenge(String codeVerifier) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		byte[] bytes = codeVerifier.getBytes("US-ASCII");
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
		messageDigest.update(bytes, 0, bytes.length);
		byte[] digest = messageDigest.digest();
		return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
	}
}
