package ch.elexis.core.ee.openapi;

import java.io.IOException;

import org.apache.hc.client5.http.impl.cache.CacheConfig;
import org.apache.hc.client5.http.impl.cache.CachingHttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.eenv.AccessToken;
import ch.elexis.core.eenv.IElexisEnvironmentService;
import ch.elexis.core.services.IContextService;
import ch.myelexis.server.api.UserApi;
import ch.myelexis.server.client.ApiClient;
import ch.myelexis.server.client.Configuration;

@Component(immediate = true, property = { EventConstants.EVENT_TOPIC + "=" + ElexisEventTopics.BASE_CONFIG + "*",
		EventConstants.EVENT_TOPIC + "=info/elexis/system/clear-cache" })
public class OpenApiClientProvider implements EventHandler {

	@Reference
	IContextService contextService;

	@Reference
	IElexisEnvironmentService elexisEnvironmentService;

	private ExtendedBasicHttpCacheStorage httpCacheStorage;
	private CloseableHttpClient httpClient;

	@Activate
	public void activate() throws IOException {

		CachingHttpClientBuilder cachingHttpClientBuilder = CachingHttpClientBuilder.create();
		CacheConfig cacheConfig = CacheConfig.custom().setSharedCache(false).build();
		httpCacheStorage = new ExtendedBasicHttpCacheStorage(cacheConfig);
		cachingHttpClientBuilder.setCacheConfig(cacheConfig);
		cachingHttpClientBuilder.setHttpCacheStorage(httpCacheStorage);
		CloseableHttpClient httpClient = cachingHttpClientBuilder.build();

		// TODO longer cache times? Provide button to clear cache?
		// TODO possible use okhttp client?
		// https://openapi-generator.tech/docs/generators/java/
		// https://www.wiremock.io/post/java-http-client-comparison
		ApiClient defaultApiClient = Configuration.getDefaultApiClient();
		defaultApiClient.setHttpClient(httpClient);
		defaultApiClient.setBasePath("https://" + elexisEnvironmentService.getHostname());
		defaultApiClient.setConnectTimeout(1000);
		defaultApiClient.setBearerToken(() -> {
			String accessToken = contextService.getTyped(AccessToken.class).map(AccessToken::getToken).orElse(null);
			if (accessToken == null) {
				LoggerFactory.getLogger(getClass()).error("No AccessToken");
			}
			return accessToken;
		});
		Configuration.setDefaultApiClient(defaultApiClient);

		UserApi userApi = new UserApi(defaultApiClient);
		FrameworkUtil.getBundle(OpenApiClientProvider.class).getBundleContext().registerService(UserApi.class, userApi,
				null);
	}

	@Deactivate
	public void deactivate() {
		if (httpClient != null) {
			try {
				httpClient.close();
			} catch (IOException e) {
				LoggerFactory.getLogger(getClass()).warn("Error closing http client", e);
			}
		}
	}

	@Override
	public void handleEvent(Event event) {
		switch (event.getTopic()) {
		case ("info/elexis/config/delete"):
		case ("info/elexis/config/update"):
		case ("info/elexis/system/clear-cache"):
			httpCacheStorage.clearCache();
			break;
		default:
			break;
		}

		// FIXME user change must evict cache
		System.out.println(event.getTopic());

	}

}
