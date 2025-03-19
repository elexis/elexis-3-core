package ch.elexis.core.services.oauth2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ch.elexis.core.eenv.AccessToken;
import ch.elexis.core.status.ObjectStatus;

public class AccessTokenUtil {

	/**
	 *
	 * @param obtainAccessToken
	 * @return
	 * @see https://metamug.com/article/security/decode-jwt-java.html
	 */
	public static AccessToken load(KeycloakAccessTokenResponse obtainAccessToken) {
		return load(obtainAccessToken, null, null);
	}

	public static AccessToken load(KeycloakAccessTokenResponse obtainAccessToken, String tokenEndpoint,
			String clientId) {
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
				refreshToken, refreshTokenExpirationDate, tokenEndpoint, clientId);
		return keycloakAccessToken;
	}

	public static ObjectStatus<AccessToken> invokeRefresh(AccessToken accessToken) {
		String tokenEndpoint = accessToken.getTokenEndpoint();
		if (tokenEndpoint != null) {

			final HttpPost httpPost = new HttpPost(tokenEndpoint);
			final List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("grant_type", "refresh_token"));
			params.add(new BasicNameValuePair("client_id", accessToken.getClientId()));
			params.add(new BasicNameValuePair("refresh_token", accessToken.getRefreshToken()));
			httpPost.setEntity(new UrlEncodedFormEntity(params));

			try (CloseableHttpClient client = HttpClients.createDefault()) {
				return client.execute(httpPost, res -> {
					if (res.getCode() == 200) {
						HttpEntity entity = res.getEntity();
						String accessTokenResponse = EntityUtils.toString(entity, "UTF-8");
						KeycloakAccessTokenResponse kcAccessTokenResponse = new Gson().fromJson(accessTokenResponse,
								KeycloakAccessTokenResponse.class);
						AccessToken _accessToken = load(kcAccessTokenResponse, tokenEndpoint,
								accessToken.getClientId());
						return ObjectStatus.OK(_accessToken);
					}
					return ObjectStatus.ERROR(res.getCode() + " " + res.getReasonPhrase());
				});

			} catch (IOException e) {
				return ObjectStatus.ERROR(e.getMessage());
			}

		}
		return ObjectStatus.ERROR("No token endpoint defined in existing access token");
	}

	private static String decode(String encodedString) {
		return new String(Base64.getUrlDecoder().decode(encodedString));
	}

}
