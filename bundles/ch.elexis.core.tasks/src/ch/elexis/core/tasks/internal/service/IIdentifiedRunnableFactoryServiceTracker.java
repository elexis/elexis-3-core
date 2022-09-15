package ch.elexis.core.tasks.internal.service;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.tasks.IIdentifiedRunnableFactory;

public class IIdentifiedRunnableFactoryServiceTracker
		extends ServiceTracker<IIdentifiedRunnableFactory, IIdentifiedRunnableFactory> {

	private Logger logger;
	private TaskServiceImpl taskService;

	public IIdentifiedRunnableFactoryServiceTracker(TaskServiceImpl taskService) {
		super(FrameworkUtil.getBundle(IIdentifiedRunnableFactoryServiceTracker.class).getBundleContext(),
				IIdentifiedRunnableFactory.class, null);
		this.taskService = taskService;
		this.logger = LoggerFactory.getLogger(getClass());
	}

	@Override
	public IIdentifiedRunnableFactory addingService(ServiceReference<IIdentifiedRunnableFactory> reference) {
		IIdentifiedRunnableFactory service = resolveContext(reference.getClass()).getService(reference);
		try {
			taskService.bindRunnableWithContextFactory(service);
		} catch (Exception e) {
			logger.error("Error binding [{}], skipping.", service != null ? service.getClass().getName() : "null", e);
		}
		return service;
	}

	@Override
	public void removedService(ServiceReference<IIdentifiedRunnableFactory> reference,
			IIdentifiedRunnableFactory service) {
		taskService.unbindRunnableWithContextFactory(service);
		resolveContext(reference.getClass()).ungetService(reference);
	}

	private BundleContext resolveContext(@SuppressWarnings("rawtypes") Class clazz) {
		Bundle bundle = FrameworkUtil.getBundle(clazz);
		// fallback to our context ...
		if (bundle == null || bundle.getBundleContext() == null) {
			bundle = FrameworkUtil.getBundle(IIdentifiedRunnableFactoryServiceTracker.class);
		}
		return bundle.getBundleContext();
	}

}
