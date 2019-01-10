package ch.elexis.core.ui.util;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.swt.widgets.Control;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

@Component(property = EventConstants.EVENT_TOPIC + "=" + UIEvents.UILifeCycle.APP_STARTUP_COMPLETE)
public class CoreUiUtil implements EventHandler {
	
	private static IEclipseContext applicationContext;
	
	@Override
	public void handleEvent(Event event){
		Object property = event.getProperty("org.eclipse.e4.data");
		if (property instanceof MApplication) {
			MApplication application = (MApplication) property;
			CoreUiUtil.applicationContext = application.getContext();
		}
	}
	
	public static void injectServices(Object object){
		ContextInjectionFactory.inject(object, applicationContext);
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
}
