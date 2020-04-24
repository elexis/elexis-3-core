package ch.elexis.core.ui.e4.util;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.swt.widgets.Control;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.services.IContextService;

@Component(service = {})
public class CoreUiUtil {
	
	private static Logger logger = LoggerFactory.getLogger(CoreUiUtil.class);
	
	private static Object lock = new Object();
	
	private static List<Object> delayedInjection = new ArrayList<>();
	
	private static IContextService contextService;
	
	@Reference
	public void setModelService(IContextService contextService){
		CoreUiUtil.contextService = contextService;
	}
	
	@Inject
	@Optional
	public void subscribeAppStartupComplete(
		@UIEventTopic(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE) Event event, UISynchronize sync){
		
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
	
	private static java.util.Optional<IEclipseContext> getApplicationContext(){
		if (contextService == null) {
			return java.util.Optional.empty();
		}
		return java.util.Optional.ofNullable((IEclipseContext) contextService.getRootContext()
			.getNamed("applicationContext").orElse(null));
	}
	
	private static void injectServices(Object object){
		if (getApplicationContext().isPresent()) {
			try {
				ContextInjectionFactory.inject(object, getApplicationContext().get());
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
			if (getApplicationContext().isPresent()) {
				injectServices(object);
			} else {
				delayedInjection.add(object);
			}
		}
	}
}
