package ch.elexis.core.services.internal;

import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import com.google.gson.Gson;

@Component(immediate = true)
public class GsonProviderService {

	private ServiceRegistration<Gson> service;

	@Activate
	public void activate() {
		Gson gson = new Gson();
		service = FrameworkUtil.getBundle(GsonProviderService.class).getBundleContext().registerService(Gson.class,
				gson, null);
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
