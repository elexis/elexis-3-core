package ch.elexis.core.services.eenv;

import java.util.Base64;
import java.util.Date;

import org.keycloak.representations.AccessTokenResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ch.elexis.core.eenv.AccessToken;

public class AccessTokenUtil {

	/**
	 *
	 * @param obtainAccessToken
	 * @return
	 * @see https://metamug.com/article/security/decode-jwt-java.html
	 */
	static AccessToken load(AccessTokenResponse obtainAccessToken) {
		Gson gson = new Gson();

		String accessToken = obtainAccessToken.getToken();
		String[] accessTokenParts = accessToken.split("\\.");
		JsonObject payload = gson.fromJson(decode(accessTokenParts[1]), JsonObject.class);

		long accessTokenExp = payload.get("exp").getAsLong();
		Date accessTokenExpirationTime = new Date(accessTokenExp * 1000);
		String username = payload.get("preferred_username").getAsString();

		String refreshToken = obtainAccessToken.getRefreshToken();
		Date refreshTokenExpirationDate = null;
		if (refreshToken != null) {
			String[] refreshTokenParts = refreshToken.split("\\.");
			JsonObject refreshTokenPayload = gson.fromJson(decode(refreshTokenParts[1]), JsonObject.class);
			long refreshTokenExp = refreshTokenPayload.get("exp").getAsLong();
			refreshTokenExpirationDate = new Date(refreshTokenExp * 1000);
		}

		AccessToken keycloakAccessToken = new AccessToken(accessToken, accessTokenExpirationTime, username,
				refreshToken, refreshTokenExpirationDate);
		return keycloakAccessToken;
	}

	private static String decode(String encodedString) {
		return new String(Base64.getUrlDecoder().decode(encodedString));
	}

}
