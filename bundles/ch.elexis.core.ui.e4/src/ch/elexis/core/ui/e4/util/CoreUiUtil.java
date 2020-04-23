package ch.elexis.core.ui.e4.util;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.swt.widgets.Control;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.services.IContextService;

public class CoreUiUtil {
	
	private static Logger logger = LoggerFactory.getLogger(CoreUiUtil.class);
	
	private static Object lock = new Object();
	
	private static IEclipseContext serviceContext;
	
	private static List<Object> delayedInjection = new ArrayList<>();
	
	@Inject
	@Optional
	public void subscribeAppStartupComplete(
		@UIEventTopic(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE) Event event,
		IContextService contextService, UISynchronize sync){
		
		synchronized (lock) {
			Object property = event.getProperty("org.eclipse.e4.data");
			if (property instanceof MApplication) {
				MApplication application = (MApplication) property;
				// A RAP application has one application context per client
				// the resp. context service implementation considers this
				contextService.getRootContext().setNamed("applicationContext",
					application.getContext());
				
				if (!delayedInjection.isEmpty()) {
					for (Object object : delayedInjection) {
						sync.asyncExec(() -> {
							injectServices(object);
						});
					}
					delayedInjection.clear();
				}
			}
		}
	}
	
	private static void injectServices(Object object){
		assertServiceContext();
		try {
			ContextInjectionFactory.inject(object, serviceContext);
		} catch (InjectionException e) {
			logger.warn("Service injection failure ", e);
		}
		if (getApplicationContext() != null) {
			try {
				ContextInjectionFactory.inject(object, getApplicationContext());
			} catch (InjectionException e) {
				logger.warn("Application context injection failure ", e);
			}
		}
	}
	
	private static void assertServiceContext(){
		if (serviceContext == null) {
			BundleContext bundleContext =
				FrameworkUtil.getBundle(CoreUiUtil.class).getBundleContext();
			CoreUiUtil.serviceContext = EclipseContextFactory.getServiceContext(bundleContext);
		}
	}
	
	private static IEclipseContext getApplicationContext(){
		IContextService iContextService = serviceContext.get(IContextService.class);
		return (IEclipseContext) iContextService.getRootContext().getNamed("applicationContext")
			.orElse(null);
	}
	
	public static void injectServices(Object object, IEclipseContext context){
		ContextInjectionFactory.inject(object, context);
	}
	
	/**
	 * Test if the control is not disposed and visible.
	 * 
	 * @param control
	 * @return
	 */
	public static boolean isActiveControl(Control control){
		return control != null && !control.isDisposed() && control.isVisible();
	}
	
	/**
	 * Inject services if application context is available, else injection is delayed until context
	 * is available. For usage with UI classes.
	 * 
	 * @param fixMediDisplay
	 */
	public static void injectServicesWithContext(Object object){
		assertServiceContext();
		synchronized (lock) {
			if (getApplicationContext() != null) {
				injectServices(object);
			} else {
				delayedInjection.add(object);
			}
		}
	}
}
