package ch.elexis.core.services.oauth2;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ch.elexis.core.eenv.AccessToken;
import ch.elexis.core.status.ObjectStatus;

public class OAuth2Service {

	private Gson gson = new GsonBuilder().create();

	/**
	 * @param keycloakRealmEndpoint e.g.
	 *                              "https://keycloak.medelexis.ch/realms/Medelexis"
	 * @param clientId
	 * @param clientSecret
	 * @param username
	 * @param password
	 * @return
	 */
	public ObjectStatus<AccessToken> performDirectAccessGrantFlow(URI keycloakRealmEndpoint, String clientId,
			String clientSecret, String username, char[] password) {

		URI tokenEndpoint = keycloakRealmEndpoint.resolve("protocol/openid-connect/token");
		final HttpPost httpPost = new HttpPost(tokenEndpoint);
		final List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("grant_type", "password"));
		params.add(new BasicNameValuePair("client_id", clientId));
		params.add(new BasicNameValuePair("client_secret", clientSecret));
		params.add(new BasicNameValuePair("username", username));
		params.add(new BasicNameValuePair("password", String.valueOf(password)));
		httpPost.setEntity(new UrlEncodedFormEntity(params));

		try (CloseableHttpClient client = HttpClients.createDefault()) {
			return client.execute(httpPost, res -> {
				if (res.getCode() == 200) {
					HttpEntity entity = res.getEntity();
					String accessTokenResponse = EntityUtils.toString(entity, "UTF-8");
					KeycloakAccessTokenResponse kcAccessTokenResponse = gson.fromJson(accessTokenResponse,
							KeycloakAccessTokenResponse.class);
					AccessToken accessToken = AccessTokenUtil.load(kcAccessTokenResponse, tokenEndpoint.toString(),
							clientId);
					return ObjectStatus.OK(accessToken);
				}
				return ObjectStatus.ERROR(res.getCode() + " " + res.getReasonPhrase());
			});

		} catch (IOException e) {
			return ObjectStatus.ERROR(e.getMessage());
		}
	}

	/**
	 * 
	 * @param keycloakRealmEndpoint
	 * @param clientId
	 * @param clientSecret
	 * @param verificationUrlCaller
	 * @return
	 * @see https://blog.please-open.it/posts/device_code/
	 * @see https://github.com/keycloak/keycloak-community/blob/main/design/oauth2-device-authorization-grant.md
	 */
	public ObjectStatus<AccessToken> performDeviceAuthorizationFlow(URI keycloakRealmEndpoint, String clientId,
			String clientSecret, Consumer<URI> verificationUrlCaller) {

		final HttpPost httpPost = new HttpPost(keycloakRealmEndpoint.resolve("protocol/openid-connect/auth/device"));
		final List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("client_id", clientId));
		params.add(new BasicNameValuePair("client_secret", clientSecret));
		httpPost.setEntity(new UrlEncodedFormEntity(params));

		try (CloseableHttpClient client = HttpClients.createDefault()) {

			// generate user code
			// https://blog.please-open.it/posts/device_code//#generate-a-user-code
			ObjectStatus<KeycloakOAuth2DeviceAuthorizationResponse> oAuth2DeviceAuthorizationResponseStatus = client
					.execute(httpPost, res -> {
						if (res.getCode() == 200) {
							HttpEntity entity = res.getEntity();
							String jwt = EntityUtils.toString(entity, "UTF-8");
							KeycloakOAuth2DeviceAuthorizationResponse response = gson.fromJson(jwt,
									KeycloakOAuth2DeviceAuthorizationResponse.class);
							return ObjectStatus.OK(response);
						}
						return ObjectStatus.ERROR(res.getCode() + " " + res.getReasonPhrase());
					});
			if (!oAuth2DeviceAuthorizationResponseStatus.isOK()) {
				return ObjectStatus.ERROR(oAuth2DeviceAuthorizationResponseStatus.getMessage());
			}

			KeycloakOAuth2DeviceAuthorizationResponse keycloakOAuth2DeviceAuthorizationResponse = oAuth2DeviceAuthorizationResponseStatus
					.get();
			String deviceCode = keycloakOAuth2DeviceAuthorizationResponse.getDeviceCode();
			String verificationUriComplete = keycloakOAuth2DeviceAuthorizationResponse.getVerificationUriComplete();
			verificationUrlCaller.accept(URI.create(verificationUriComplete));

			// polling for the token
			// https://blog.please-open.it/posts/device_code/#get-a-token--polling
			URI tokenEndpoint = keycloakRealmEndpoint.resolve("protocol/openid-connect/token");
			for (int i = 0; i < 15; i++) {
				Thread.sleep(2000); // Realm Settings / Token / OAuth 2.0 Device Polling Interval
				ObjectStatus<AccessToken> status = performDeviceAuthorizationFlowPollCall(tokenEndpoint, client,
						deviceCode, clientId, clientSecret);
				if (status.isOK()) {
					return ObjectStatus.OK(status.get());
				}
			}
			return ObjectStatus.ERROR("No token received after 30 seconds");
		} catch (InterruptedException | IOException e) {
			return ObjectStatus.ERROR(e.getMessage());
		}

	}

	private ObjectStatus<AccessToken> performDeviceAuthorizationFlowPollCall(URI tokenEndpoint,
			CloseableHttpClient client, String deviceCode, String clientId, String clientSecret) throws IOException {
		final HttpPost httpPost = new HttpPost(tokenEndpoint);
		final List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("device_code", deviceCode));
		params.add(new BasicNameValuePair("grant_type", "urn:ietf:params:oauth:grant-type:device_code"));
		params.add(new BasicNameValuePair("client_id", clientId));
		params.add(new BasicNameValuePair("client_secret", clientSecret));
		httpPost.setEntity(new UrlEncodedFormEntity(params));

		return client.execute(httpPost, res -> {
			if (res.getCode() == 200) {
				HttpEntity entity = res.getEntity();
				String accessTokenResponse = EntityUtils.toString(entity, "UTF-8");
				KeycloakAccessTokenResponse kcAccessTokenResponse = gson.fromJson(accessTokenResponse,
						KeycloakAccessTokenResponse.class);
				AccessToken accessToken = AccessTokenUtil.load(kcAccessTokenResponse, tokenEndpoint.toString(),
						clientId);
				return ObjectStatus.OK(accessToken);
			}
			return ObjectStatus.INFO(res.getReasonPhrase());
		});

	}

}
