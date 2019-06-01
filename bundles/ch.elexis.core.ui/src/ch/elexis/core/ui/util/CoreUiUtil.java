package ch.elexis.core.ui.util;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IImage;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.ui.UiDesk;

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
	
	public static void injectServices(Object object){
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
	
	/**
	 * Load a {@link Color} for the RGB color string. The color string is expected in hex format.
	 * 
	 * @param colorString
	 * @return
	 */
	public static Color getColorForString(String colorString){
		colorString = StringUtils.leftPad(colorString, 6, '0');
		if (!UiDesk.getColorRegistry().hasValueFor(colorString)) {
			RGB rgb;
			try {
				rgb = new RGB(Integer.parseInt(colorString.substring(0, 2), 16),
					Integer.parseInt(colorString.substring(2, 4), 16),
					Integer.parseInt(colorString.substring(4, 6), 16));
			} catch (NumberFormatException nex) {
				logger.warn("Error parsing color string [" + colorString + "]", nex);
				rgb = new RGB(100, 100, 100);
			}
			UiDesk.getColorRegistry().put(colorString, rgb);
		}
		return UiDesk.getColorRegistry().get(colorString);
	}
	
	/**
	 * Load the {@link Image} and scale it to 16x16px size.
	 * 
	 * @param image
	 * @return
	 */
	public static Image getImageAsIcon(IImage image){
		Image ret = UiDesk.getImageRegistry().get(image.getId() + "_16x16");
		if (ret == null) {
			Image origImage = getImage(image);
			ret = getImageScaledTo(origImage, 16, 16, false);
			if (ret != null) {
				UiDesk.getImageRegistry().put(image.getId() + "_16x16", ret);
			}
		}
		
		return ret;
	}
	
	/**
	 * Get the {@link Image} from the {@link IImage}.
	 * 
	 * @param image
	 * @return
	 */
	public static Image getImage(IImage image){
		Image ret = UiDesk.getImageRegistry().get(image.getId());
		if (ret == null) {
			byte[] in = image.getImage();
			ByteArrayInputStream bais = new ByteArrayInputStream(in);
			try {
				ImageData idata = new ImageData(bais);
				ret = new Image(Display.getDefault(), idata);
				if (ret != null) {
					UiDesk.getImageRegistry().put(image.getId(), ret);
				}
			} catch (Exception ex) {
				logger.error("Error loading image [" + image.getId() + "]", ex);
			}
		}
		return ret;
	}
	
	private static Image getImageScaledTo(Image orig, int width, int height, boolean bShrinkOnly){
		ImageData idata = orig.getImageData();
		if (idata.width != width || idata.height != height) {
			idata = idata.scaledTo(width, height);
		}
		Image ret = new Image(Display.getDefault(), idata);
		return ret;
	}
	
	public static Composite createForm(Composite parent, ISticker iSticker){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new GridLayout(2, false));
		
		Image img = null;
		if (iSticker.getImage() != null) {
			img = getImageAsIcon(iSticker.getImage());
		}
		GridData gd1 = null;
		GridData gd2 = null;
		
		Composite cImg = new Composite(ret, SWT.NONE);
		if (img != null) {
			cImg.setBackgroundImage(img);
			gd1 = new GridData(img.getBounds().width, img.getBounds().height);
			gd2 = new GridData(SWT.DEFAULT, img.getBounds().height);
		} else {
			gd1 = new GridData(10, 10);
			gd2 = new GridData(SWT.DEFAULT, SWT.DEFAULT);
		}
		cImg.setLayoutData(gd1);
		Label lbl = new Label(ret, SWT.NONE);
		lbl.setLayoutData(gd2);
		lbl.setText(iSticker.getLabel());
		lbl.setForeground(getColorForString(iSticker.getForeground()));
		lbl.setBackground(getColorForString(iSticker.getBackground()));
		return ret;
	}
	
	
}
