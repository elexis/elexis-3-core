package ch.elexis.core.httpclient.internal;

import org.apache.hc.client5.http.impl.cache.CacheConfig;
import org.apache.hc.client5.http.impl.cache.CachingHttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.entity.BufferedHttpEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.services.IContextService;

@Component(immediate = true, property = { EventConstants.EVENT_TOPIC + "=" + ElexisEventTopics.BASE_CONFIG + "*",
		EventConstants.EVENT_TOPIC + "=info/elexis/system/clear-cache" })
public class HttpClientProvider implements EventHandler {

	@Reference
	IContextService contextService;

	private ServiceRegistration<CloseableHttpClient> service;

	private ExtendedBasicHttpCacheStorage httpCacheStorage;
	private CloseableHttpClient closeableHttpClient;


	@Activate
	public void activate() {

		// Performance Ideas
		// no additional gson
		// http2.0 <-> myelexis-server
		// conditional operations
		// grpc instead of json?
		// add "cache-control" header to search result
		// search -> only communicate references + labels -> then perform parallel
		// direct async loads
		// Subscription?
		// e.g. Pendenzen -> Nur PatientenTask nach User Event, andere bei click reload,
		// oder alle 15 sekunden im hintergrund

		CachingHttpClientBuilder cachingHttpClientBuilder = CachingHttpClientBuilder.create();
		CacheConfig cacheConfig = CacheConfig.custom().setSharedCache(false).build();
		httpCacheStorage = new ExtendedBasicHttpCacheStorage(cacheConfig);
		cachingHttpClientBuilder.setCacheConfig(cacheConfig);
		cachingHttpClientBuilder.setHttpCacheStorage(httpCacheStorage);

		// see https://github.com/hapifhir/hapi-fhir/issues/7263
		cachingHttpClientBuilder.addResponseInterceptorFirst(
				(final HttpResponse response, final EntityDetails details, final HttpContext ctx) -> {
					if (response instanceof final HttpEntityContainer container) {
						final HttpEntity entity = container.getEntity();
						if (entity != null && !entity.isRepeatable()) {
							container.setEntity(new BufferedHttpEntity(entity)); // consumes & makes repeatable
						}
					}
				});

		String eeHostname = System.getProperty(ElexisSystemPropertyConstants.EE_HOSTNAME);
		if (eeHostname != null) {
			cachingHttpClientBuilder.addRequestInterceptorFirst(new EEAuthInterceptor(eeHostname, contextService));
		}
		closeableHttpClient = cachingHttpClientBuilder.build();

		service = FrameworkUtil.getBundle(HttpClientProvider.class).getBundleContext()
				.registerService(CloseableHttpClient.class, closeableHttpClient, null);
	}

	@Deactivate
	public void deactivate() {
		if (service == null) {
			return;
		}
		try {
			closeableHttpClient.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		service.unregister();
		service = null;
	}

	@Override
	public void handleEvent(Event event) {
		switch (event.getTopic()) {
		case ("info/elexis/config/delete"):
		case ("info/elexis/config/update"):
		case ("info/elexis/system/clear-cache"):
			// FIXME can only clear whole cache - no good if only specific key needs to be
			// cleared
			// e.g. userconfig.set
			httpCacheStorage.clearCache();
			break;
		default:
			break;
		}

		// FIXME user change must evict cache
		System.out.println(event.getTopic());

	}
}
