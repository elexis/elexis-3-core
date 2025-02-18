package ch.elexis.core.services.eenv;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Timer;

import org.apache.commons.lang3.StringUtils;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.representations.AccessTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ch.elexis.core.eenv.AccessToken;
import ch.elexis.core.eenv.IElexisEnvironmentService;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.time.TimeUtil;

// Activated via ElexisEnvironmentServiceActivator
public class ElexisEnvironmentService implements IElexisEnvironmentService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private String elexisEnvironmentHost;
	private IContextService contextService;
	private IConfigService configService;

	private KeycloakDeployment keycloakDeployment;
	private Timer refreshAccessTokenTimer;

	public ElexisEnvironmentService(String elexisEnvironmentHost, IContextService contextService,
			IConfigService configService) {
		this.elexisEnvironmentHost = elexisEnvironmentHost;
		this.contextService = contextService;
		this.configService = configService;

		LoggerFactory.getLogger(getClass()).info("Binding to EE {}", getHostname());

		keycloakDeployment = KeycloakDeploymentBuilder.build(getKeycloakConfiguration());
		refreshAccessTokenTimer = new Timer("Refresh EE access-token", true); //$NON-NLS-1$
		refreshAccessTokenTimer.schedule(new RefreshAccessTokenTimerTask(keycloakDeployment, contextService), 60 * 1000,
				60 * 1000);
	}

	@Override
	public String getVersion() {
		return "unknown";
	}

	@Override
	public String getProperty(String key) {
		// 1. check for environment variables
		String value = System.getenv(key);
		if (StringUtils.isNotEmpty(value)) {
			return value;
		}

		// TODO first try via LocalProperties?
		// THEN Config DB Table ?
		return configService.get(key, null);
	}

	@Override
	public JsonObject getStatus() {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(getBaseUrl() + "/.status.json")).build();

		HttpResponse<String> response;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofString());
			return JsonParser.parseString(response.body()).getAsJsonObject();
		} catch (IOException | InterruptedException e) {
			logger.warn("Error obtaining status", e);
		}

		return null;
	}

	@Override
	public String getHostname() {
		return elexisEnvironmentHost;
	}

	@Override
	public void loadAccessToken(String username, char[] password) {
		try {
			AuthzClient authzClient = AuthzClient.create(getKeycloakConfiguration());
			AccessTokenResponse obtainAccessToken = authzClient.obtainAccessToken(username, String.valueOf(password));
			AccessToken keycloakAccessToken = AccessTokenUtil.load(obtainAccessToken);

			contextService.getRootContext().setTyped(keycloakAccessToken);
			logger.info("Loaded access-token for [{}], valid until [{}], refresh until [{}]",
					keycloakAccessToken.getUsername(),
					TimeUtil.toLocalDateTime(keycloakAccessToken.getAccessTokenExpiration()),
					TimeUtil.toLocalDateTime(keycloakAccessToken.refreshTokenExpiration()));
		} catch (Exception e) {
			logger.warn("Error obtaining access token", e);
			return;
		}
	}

	private Configuration getKeycloakConfiguration() {
		Configuration keycloakConfiguration = new Configuration();
		keycloakConfiguration.setRealm(IElexisEnvironmentService.EE_KEYCLOAK_REALM_ID);
		keycloakConfiguration.setAuthServerUrl(getKeycloakBaseUrl() + "/auth");
		keycloakConfiguration.setResource("elexis-rcp-openid");
		keycloakConfiguration.setPublicClient(false);
		keycloakConfiguration.setDisableTrustManager(false);
		String rcpClientSecret = getProperty(IElexisEnvironmentService.EE_RCP_OPENID_SECRET);
		keycloakConfiguration.getCredentials().put("secret", rcpClientSecret);
		return keycloakConfiguration;
	}

}
