package ch.elexis.core.ee.openapi;

import java.io.IOException;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.eenv.IElexisEnvironmentService;
import ch.elexis.core.services.IContextService;
import ch.myelexis.server.api.EntityManagementApi;
import ch.myelexis.server.api.UserApi;
import ch.myelexis.server.client.ApiClient;
import ch.myelexis.server.client.Configuration;

@Component
public class OpenApiClientProvider {

	@Reference
	IContextService contextService;

	@Reference
	IElexisEnvironmentService elexisEnvironmentService;

	@Reference
	CloseableHttpClient httpClient;

	@Activate
	public void activate() throws IOException {

		// TODO longer cache times? Provide button to clear cache?
		// TODO possible use okhttp client?
		// https://openapi-generator.tech/docs/generators/java/
		// https://www.wiremock.io/post/java-http-client-comparison
		ApiClient defaultApiClient = Configuration.getDefaultApiClient();
		defaultApiClient.setHttpClient(httpClient);
		defaultApiClient.setBasePath("https://" + elexisEnvironmentService.getHostname());
		defaultApiClient.setConnectTimeout(1000);
//		defaultApiClient.setBearerToken(() -> {
//			String accessToken = contextService.getTyped(AccessToken.class).map(AccessToken::getToken).orElse(null);
//			if (accessToken == null) {
//				LoggerFactory.getLogger(getClass()).error("No AccessToken");
//			}
//			return accessToken;
//		});
		Configuration.setDefaultApiClient(defaultApiClient);

		UserApi userApi = new UserApi(defaultApiClient);
		FrameworkUtil.getBundle(OpenApiClientProvider.class).getBundleContext().registerService(UserApi.class, userApi,
				null);

		EntityManagementApi entityManagementApi = new EntityManagementApi(defaultApiClient);
		FrameworkUtil.getBundle(OpenApiClientProvider.class).getBundleContext()
				.registerService(EntityManagementApi.class, entityManagementApi, null);
	}

}
