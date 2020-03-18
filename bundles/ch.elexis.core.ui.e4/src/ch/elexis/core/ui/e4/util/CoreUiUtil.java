package ch.elexis.core.ui.e4.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(property = EventConstants.EVENT_TOPIC + "=" + UIEvents.UILifeCycle.APP_STARTUP_COMPLETE)
public class CoreUiUtil implements EventHandler {
	
	private static Logger logger = LoggerFactory.getLogger(CoreUiUtil.class);
	
	private static Object lock = new Object();
	
	private static IEclipseContext applicationContext;
	
	private static IEclipseContext serviceContext;
	
	private static List<Object> delayedInjection = new ArrayList<>();
	
	@Override
	public void handleEvent(Event event){
		synchronized (lock) {
			Object property = event.getProperty("org.eclipse.e4.data");
			if (property instanceof MApplication) {
				MApplication application = (MApplication) property;
				CoreUiUtil.applicationContext = application.getContext();
				
				if (!delayedInjection.isEmpty()) {
					for (Object object : delayedInjection) {
						Display.getDefault().asyncExec(() -> {
							injectServices(object);
						});
					}
					delayedInjection.clear();
				}
			}
		}
	}
	
	private static void injectServices(Object object){
		if (serviceContext == null) {
			BundleContext bundleContext =
				FrameworkUtil.getBundle(CoreUiUtil.class).getBundleContext();
			CoreUiUtil.serviceContext = EclipseContextFactory.getServiceContext(bundleContext);
		}
		try {
			ContextInjectionFactory.inject(object, serviceContext);
		} catch (InjectionException e) {
			logger.warn("Service injection failure ", e);
		}
		if (applicationContext != null) {
			try {
				ContextInjectionFactory.inject(object, applicationContext);
			} catch (InjectionException e) {
				logger.warn("Application context injection failure ", e);
			}
		}
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
		synchronized (lock) {
			if (applicationContext != null) {
				injectServices(object);
			} else {
				delayedInjection.add(object);
			}
		}
	}
}
