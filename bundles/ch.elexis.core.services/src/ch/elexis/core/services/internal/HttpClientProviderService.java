package ch.elexis.core.services.internal;

import java.net.http.HttpClient;

import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component(immediate = true)
public class HttpClientProviderService {

	private ServiceRegistration<HttpClient> service;

	@Activate
	public void activate() {
		HttpClient client = HttpClient.newHttpClient();
		service = FrameworkUtil.getBundle(HttpClientProviderService.class).getBundleContext()
				.registerService(HttpClient.class, client, null);
	}

	@Deactivate
	public void deactivate() {
		if (service == null) {
			return;
		}
		service.unregister();
		service = null;
	}
}