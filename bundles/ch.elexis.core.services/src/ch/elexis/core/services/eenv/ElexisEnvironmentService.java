package ch.elexis.core.services.eenv;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.IOUtils;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.representations.AccessTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ch.elexis.core.eenv.AccessToken;
import ch.elexis.core.eenv.IElexisEnvironmentService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.holder.ConfigServiceHolder;

// Activated via ElexisEnvironmentServiceActivator
public class ElexisEnvironmentService implements IElexisEnvironmentService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private String elexisEnvironmentHost;
	private IContextService contextService;

	private CompletableFuture<Map<String, Object>> eeStatus;

	@SuppressWarnings("unchecked")
	public ElexisEnvironmentService(String elexisEnvironmentHost, IContextService contextService) {
		this.elexisEnvironmentHost = elexisEnvironmentHost;
		this.contextService = contextService;

		LoggerFactory.getLogger(getClass()).info("Binding to EE {}", getHostname());
		eeStatus = CompletableFuture.supplyAsync(() -> {
			try (InputStream is = new URL("https://" + elexisEnvironmentHost + "/.status.json").openStream()) {
				String json = IOUtils.toString(is, "UTF-8");
				return new Gson().fromJson(json, Map.class);
			} catch (IOException e) {
				logger.warn("Could not load status.json", e);
				return Collections.emptyMap();
			}
		});
	}

	@SuppressWarnings("rawtypes")
	@Override
	public String getVersion() {
		try {
			Map eeMap = (Map) eeStatus.get().get("ee");
			if (eeMap instanceof Map) {
				Map eeMapGit = (Map) eeMap.get("git");
				if (eeMapGit instanceof Map) {
					return (String) eeMapGit.get("branch");
				}
			}
		} catch (InterruptedException | ExecutionException e) {
			logger.warn("", e);
		}
		return "unknown";
	}

	@Override
	public String getProperty(String key) {
		// TODO EnvironmentVariables?
		// TODO first try via LocalProperties?
		// THEN Config DB Table ?
		return ConfigServiceHolder.get().get(key, null);
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
			AccessToken keycloakAccessToken = AccessTokenUtil.load(obtainAccessToken.getToken());

			contextService.getRootContext().setTyped(keycloakAccessToken);
			logger.info("Loaded access-token for user [{}], valid until [{}]", keycloakAccessToken.getUsername(),
					keycloakAccessToken.getExpirationTime());
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
