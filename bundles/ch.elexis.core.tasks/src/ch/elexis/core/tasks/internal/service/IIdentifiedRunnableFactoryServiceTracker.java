package ch.elexis.core.tasks.internal.service;

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
		IIdentifiedRunnableFactory service = super.addingService(reference);
		try {
			taskService.bindRunnableWithContextFactory(service);
		} catch (Exception e) {
			logger.error("Error binding [{}], skipping.", service.getClass().getName(), e);
		}
		return service;
	}

	@Override
	public void removedService(ServiceReference<IIdentifiedRunnableFactory> reference,
			IIdentifiedRunnableFactory service) {
		taskService.unbindRunnableWithContextFactory(service);
		super.removedService(reference, service);
	}

}
