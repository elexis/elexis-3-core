package ch.elexis.core.services.oauth2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
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

import ch.elexis.core.ee.OpenIdUser;
import ch.elexis.core.ee.json.KeycloakAccessTokenJwt;
import ch.elexis.core.eenv.AccessToken;
import ch.elexis.core.model.IUser;
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
		KeycloakAccessTokenJwt accessTokenJwt = gson.fromJson(decode(accessTokenParts[1]),
				KeycloakAccessTokenJwt.class);

		Date issuedAtDate = new Date(accessTokenJwt.iat * 1000);

		Date accessTokenExpirationTime = new Date(accessTokenJwt.exp * 1000);

		String refreshToken = obtainAccessToken.getRefreshToken();
		Date refreshTokenExpirationDate = null;
		if (refreshToken != null) {
			String[] refreshTokenParts = refreshToken.split("\\.");
			JsonObject refreshTokenPayload = gson.fromJson(decode(refreshTokenParts[1]), JsonObject.class);
			long refreshTokenExp = refreshTokenPayload.get("exp").getAsLong();
			refreshTokenExpirationDate = new Date(refreshTokenExp * 1000);
		}

		AccessToken keycloakAccessToken = new AccessToken(accessToken, issuedAtDate, accessTokenExpirationTime,
				accessTokenJwt.preferredUsername, refreshToken, refreshTokenExpirationDate, tokenEndpoint, clientId);

		return keycloakAccessToken;
	}

	/**
	 * Generate an {@link IUser} out of the information provided in the AccessToken.
	 * Throws exception if token does not provide required information.
	 * 
	 * @param accessToken
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static IUser validateCreateIUser(AccessToken accessToken) {
		String _accessToken = accessToken.getToken();
		String[] accessTokenParts = _accessToken.split("\\.");
		KeycloakAccessTokenJwt accessTokenJwt = new Gson().fromJson(decode(accessTokenParts[1]),
				KeycloakAccessTokenJwt.class);

		String preferredUsername = accessToken.getUsername();
		String name = accessTokenJwt.givenName;
		String familyName = accessTokenJwt.familyName;
		String associatedContactId = accessTokenJwt.associatedContactId;
		Set<String> roles = Set.of(accessTokenJwt.realmAccess.roles);

		if (StringUtils.isBlank(associatedContactId)) {
			throw new IllegalArgumentException("User has no associated contact");
		}

		return new OpenIdUser(preferredUsername, name, familyName, accessToken.getTokenIssuedAt().getTime(),
				accessToken.getAccessTokenExpiration().getTime(), associatedContactId, roles);
	}

	public static ObjectStatus<AccessToken> invokeRefresh(AccessToken accessToken, String clientSecret) {
		String tokenEndpoint = accessToken.getTokenEndpoint();
		if (tokenEndpoint != null) {

			final HttpPost httpPost = new HttpPost(tokenEndpoint);
			final List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("grant_type", "refresh_token"));
			params.add(new BasicNameValuePair("client_id", accessToken.getClientId()));
			if (StringUtils.isNotBlank(clientSecret)) {
				params.add(new BasicNameValuePair("client_secret", clientSecret));
			}
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
