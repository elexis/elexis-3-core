package ch.elexis.core.services.eenv;

import org.apache.commons.lang3.StringUtils;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.representations.AccessTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.eenv.AccessToken;
import ch.elexis.core.eenv.IElexisEnvironmentService;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IContextService;

// Activated via ElexisEnvironmentServiceActivator
public class ElexisEnvironmentService implements IElexisEnvironmentService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private String elexisEnvironmentHost;
	private IContextService contextService;
	private IConfigService configService;

//	private CompletableFuture<Map<String, Object>> eeStatus;

	public ElexisEnvironmentService(String elexisEnvironmentHost, IContextService contextService,
			IConfigService configService) {
		this.elexisEnvironmentHost = elexisEnvironmentHost;
		this.contextService = contextService;
		this.configService = configService;

		LoggerFactory.getLogger(getClass()).info("Binding to EE {}", getHostname());
//		eeStatus = CompletableFuture.supplyAsync(() -> {
//			// FIXME WHAT IF NOT READY SET??
//			try (InputStream is = new URL(getBaseUrl() + "/.status.json").openStream()) {
//				String json = IOUtils.toString(is, "UTF-8");
//				return new Gson().fromJson(json, Map.class);
//			} catch (IOException e) {
//				logger.warn("Could not load status.json", e);
//				return Collections.emptyMap();
//			}
//		});
	}

	@Override
	public String getVersion() {
//		try {
//			Map eeMap = (Map) eeStatus.get().get("ee");
//			if (eeMap instanceof Map) {
//				Map eeMapGit = (Map) eeMap.get("git");
//				if (eeMapGit instanceof Map) {
//					return (String) eeMapGit.get("branch");
//				}
//			}
//		} catch (InterruptedException | ExecutionException e) {
//			logger.warn("", e);
//		}
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
