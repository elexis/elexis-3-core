package ch.elexis.core.ee.openapi;

import java.time.Duration;

import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ch.elexis.core.eenv.AccessToken;
import ch.elexis.core.eenv.IElexisEnvironmentService;
import ch.elexis.core.services.IContextService;
import ch.myelexis.server.api.UserApi;
import ch.myelexis.server.client.ApiClient;
import ch.myelexis.server.client.Configuration;

@Component(immediate = true)
public class OpenApiClientInitializer {

	@Reference
	IContextService contextService;

	@Reference
	IElexisEnvironmentService elexisEnvironmentService;

	@Activate
	public void activate() {

		ApiClient defaultApiClient = Configuration.getDefaultApiClient();

		defaultApiClient.setHost(elexisEnvironmentService.getHostname());
		defaultApiClient.setScheme("https");
		defaultApiClient.setConnectTimeout(Duration.ofSeconds(1));
		defaultApiClient.setRequestInterceptor(builder -> {
			String accessToken = contextService.getTyped(AccessToken.class).map(AccessToken::getToken).orElse(null);
			if (accessToken == null) {
				LoggerFactory.getLogger(getClass()).error("No AccessToken");
			}
			builder.setHeader("Authorization", "Bearer " + accessToken);
		});

		Configuration.setDefaultApiClient(defaultApiClient);

		UserApi userApi = new UserApi(defaultApiClient);

		FrameworkUtil.getBundle(OpenApiClientInitializer.class).getBundleContext().registerService(UserApi.class,
				userApi, null);

	}

}
