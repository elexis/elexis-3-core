package ch.elexis.core.services.eenv;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Timer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ch.elexis.core.ee.json.WellKnownRcp;
import ch.elexis.core.eenv.AccessToken;
import ch.elexis.core.eenv.IElexisEnvironmentService;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.oauth2.OAuth2Service;
import ch.elexis.core.services.oauth2.RefreshAccessTokenTimerTask;
import ch.elexis.core.status.ObjectStatus;
import ch.elexis.core.time.TimeUtil;
import ch.elexis.core.utils.OsgiServiceUtil;

// Activated via ElexisEnvironmentServiceActivator
public class ElexisEnvironmentService implements IElexisEnvironmentService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private String elexisEnvironmentHost;
	private IContextService contextService;
	private IConfigService configService;

	private Timer refreshAccessTokenTimer;

	public ElexisEnvironmentService(String elexisEnvironmentHost, IContextService contextService,
			IConfigService configService) {
		this.elexisEnvironmentHost = elexisEnvironmentHost;
		this.contextService = contextService;
		this.configService = configService;

		LoggerFactory.getLogger(getClass()).info("Binding to EE {}", getHostname());

		refreshAccessTokenTimer = new Timer("Refresh EE access-token", true); //$NON-NLS-1$
		refreshAccessTokenTimer.schedule(new RefreshAccessTokenTimerTask(contextService, this), 60 * 1000, 60 * 1000);
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
	public WellKnownRcp getWellKnownRcp() {
		HttpClient client = OsgiServiceUtil.getService(HttpClient.class).get();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(getBaseUrl() + "/.well-known/elexis-rcp"))
				.build();
		try {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			return new Gson().fromJson(response.body(), WellKnownRcp.class);
		} catch (IOException | InterruptedException e) {
			logger.warn("Error obtaining /.well-known/elexis-rcp returning defaults", e);
		} finally {
			OsgiServiceUtil.ungetService(client);
		}

		return new WellKnownRcp();
	}

	@Override
	public JsonObject getStatus() {
		HttpClient client = OsgiServiceUtil.getService(HttpClient.class).get();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(getBaseUrl() + "/.status.json")).build();

		HttpResponse<String> response;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofString());
			return JsonParser.parseString(response.body()).getAsJsonObject();
		} catch (IOException | InterruptedException e) {
			logger.warn("Error obtaining status", e);
		} finally {
			OsgiServiceUtil.ungetService(client);
		}

		return null;
	}

	@Override
	public String getHostname() {
		return elexisEnvironmentHost;
	}

	@Override
	public void loadAccessToken(String username, char[] password) {
		String rcpClientSecret = getProperty(IElexisEnvironmentService.EE_RCP_OPENID_SECRET);
		ObjectStatus<AccessToken> accessToken = new OAuth2Service().performDirectAccessGrantFlow(
				URI.create(getKeycloakRealmEndpoint()), "elexis-rcp-openid", rcpClientSecret, username, password);
		if (accessToken.isOK()) {
			contextService.getRootContext().setTyped(accessToken.getObject());
			logger.info("Loaded access-token for [{}], valid until [{}], refresh until [{}]",
					accessToken.getObject().getUsername(),
					TimeUtil.toLocalDateTime(accessToken.getObject().getAccessTokenExpiration()),
					TimeUtil.toLocalDateTime(accessToken.getObject().refreshTokenExpiration()));
		} else {
			logger.warn("Could not load accessToken: " + accessToken.getMessage());
		}
	}

}
