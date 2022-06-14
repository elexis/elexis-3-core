package ch.elexis.core.services.eenv;

import java.util.Base64;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ch.elexis.core.eenv.AccessToken;

public class AccessTokenUtil {

	/**
	 *
	 * @param token
	 * @return
	 * @see https://metamug.com/article/security/decode-jwt-java.html
	 */
	static AccessToken load(String token) {
		Gson gson = new Gson();

		String[] parts = token.split("\\.");
//		JsonObject header = gson.fromJson(decode(parts[0]), JsonObject.class);
		JsonObject payload = gson.fromJson(decode(parts[1]), JsonObject.class);
//		String signature = decode(parts[2]);

		long exp = payload.get("exp").getAsLong();
		Date expirationTime = new Date(exp * 1000);
		String username = payload.get("preferred_username").getAsString();

		AccessToken keycloakAccessToken = new AccessToken(token, expirationTime, username);
		return keycloakAccessToken;
	}

	private static String decode(String encodedString) {
		return new String(Base64.getUrlDecoder().decode(encodedString));
	}

}
