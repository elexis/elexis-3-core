package ch.elexis.core.ui.e4.util;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
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

@Component(property = EventConstants.EVENT_TOPIC + "=" + UIEvents.UILifeCycle.APP_STARTUP_COMPLETE)
public class CoreUiUtil implements EventHandler {

	private static Logger logger = LoggerFactory.getLogger(CoreUiUtil.class);

	private static Object lock = new Object();

	private static List<Object> delayedInjection = new ArrayList<>();

	private static IEclipseContext applicationContext;

	private static IEclipseContext serviceContext;

	@Override
	public void handleEvent(Event event) {
		logger.info("APPLICATION STARTUP COMPLETE"); //$NON-NLS-1$
		synchronized (lock) {
			Object property = event.getProperty("org.eclipse.e4.data"); //$NON-NLS-1$
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

	private static IEclipseContext getServiceContext() {
		BundleContext bundleContext = FrameworkUtil.getBundle(CoreUiUtil.class).getBundleContext();
		return EclipseContextFactory.getServiceContext(bundleContext);
	}

	public static void injectServices(Object object) {
		if (applicationContext != null) {
			try {
				ContextInjectionFactory.inject(object, applicationContext);
				return;
			} catch (InjectionException e) {
				logger.warn("Application context injection failure ", e); //$NON-NLS-1$
			}
		}
		if (serviceContext == null) {
			CoreUiUtil.serviceContext = getServiceContext();
		}
		try {
			ContextInjectionFactory.inject(object, serviceContext);
		} catch (InjectionException e) {
			logger.warn("Service injection failure ", e); //$NON-NLS-1$
		}
	}

	public static void uninjectServices(Object object) {
		if (serviceContext == null) {
			CoreUiUtil.serviceContext = getServiceContext();
		}
		try {
			ContextInjectionFactory.uninject(object, serviceContext);
		} catch (InjectionException e) {
			logger.warn("Service injection failure ", e); //$NON-NLS-1$
		}
		if (applicationContext != null) {
			try {
				ContextInjectionFactory.uninject(object, applicationContext);
			} catch (InjectionException e) {
				logger.warn("Application context injection failure ", e); //$NON-NLS-1$
			}
		}
	}

	public static void injectServices(Object object, IEclipseContext context) {
		ContextInjectionFactory.inject(object, context);
	}
	
	public static void uninjectServices(Object object, IEclipseContext context) {
		ContextInjectionFactory.uninject(object, context);
	}

	/**
	 * Test if the control is not disposed and visible.
	 *
	 * @param control
	 * @return
	 */
	public static boolean isActiveControl(Control control) {
		return control != null && !control.isDisposed() && control.isVisible();
	}

	/**
	 * Inject services if application context is available, else injection is
	 * delayed until context is available. For usage with UI classes.
	 *
	 * @param fixMediDisplay
	 */
	public static void injectServicesWithContext(Object object) {
		synchronized (lock) {
			if (applicationContext != null) {
				injectServices(object);
			} else {
				delayedInjection.add(object);
			}
		}
	}

	/**
	 * Update the part tags to enable or disable closing and moving, depending on
	 * {@link GlobalActions#fixLayoutAction} check state.
	 *
	 * @param part
	 */
	public static void updateFixLayout(MPart part, boolean state) {
		// make sure there is a change notification produced to update the ui
		part.setCloseable(state);
		part.setCloseable(!state);
		if (state) {
			if (!part.getTags().contains("NoMove")) { //$NON-NLS-1$
				part.getTags().add("NoMove"); //$NON-NLS-1$
			}
		} else {
			part.getTags().remove("NoMove"); //$NON-NLS-1$
		}
	}

	/**
	 * Load a {@link Color} for the RGB color string. The color string is expected
	 * in hex format.
	 *
	 * @param colorString
	 * @return
	 */
	public static Color getColorForString(String colorString) {
		colorString = StringUtils.leftPad(colorString, 6, '0');
		if (!JFaceResources.getColorRegistry().hasValueFor(colorString)) {
			RGB rgb;
			try {
				rgb = new RGB(Integer.parseInt(colorString.substring(0, 2), 16),
						Integer.parseInt(colorString.substring(2, 4), 16),
						Integer.parseInt(colorString.substring(4, 6), 16));
			} catch (NumberFormatException nex) {
				logger.warn("Error parsing color string [" + colorString + "]", nex); //$NON-NLS-1$ //$NON-NLS-2$
				rgb = new RGB(100, 100, 100);
			}
			JFaceResources.getColorRegistry().put(colorString, rgb);
		}
		return JFaceResources.getColorRegistry().get(colorString);
	}

	/**
	 * Load the {@link Image} and scale it to 16x16px size.
	 *
	 * @param image
	 * @return
	 */
	public static Image getImageAsIcon(IImage image) {
		Image ret = JFaceResources.getImageRegistry().get(image.getId() + "_16x16"); //$NON-NLS-1$
		if (ret == null) {
			Image origImage = getImage(image);
			ret = getImageScaledTo(origImage, 16, 16, false);
			if (ret != null) {
				JFaceResources.getImageRegistry().put(image.getId() + "_16x16", ret); //$NON-NLS-1$
			}
		}

		return ret;
	}

	private static Image getImageScaledTo(Image orig, int width, int height, boolean bShrinkOnly) {
		ImageData idata = orig.getImageData();
		if (idata.width != width || idata.height != height) {
			idata = idata.scaledTo(width, height);
		}
		Image ret = new Image(Display.getDefault(), idata);
		return ret;
	}

	/**
	 * Get the {@link Image} from the {@link IImage}.
	 *
	 * @param image
	 * @return
	 */
	public static Image getImage(IImage image) {
		Image ret = JFaceResources.getImageRegistry().get(image.getId());
		if (ret == null) {
			byte[] in = image.getImage();
			ByteArrayInputStream bais = new ByteArrayInputStream(in);
			try {
				ImageData idata = new ImageData(bais);
				ret = new Image(Display.getDefault(), idata);
				if (ret != null) {
					JFaceResources.getImageRegistry().put(image.getId(), ret);
				}
			} catch (Exception ex) {
				logger.error("Error loading image [" + image.getId() + "]", ex); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return ret;
	}

	public static Composite createForm(Composite parent, ISticker iSticker) {
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
		lbl.setForeground(CoreUiUtil.getColorForString(iSticker.getForeground()));
		lbl.setBackground(getColorForString(iSticker.getBackground()));
		return ret;
	}

	/**
	 * Retrieve the selection for the commandId (added by
	 * {@link CoreUiUtil#addCommandContributions(IMenuManager, Object[], String)})
	 * from the {@link IEclipseContext}. The named variable is removed from the
	 * context.
	 *
	 * @param iEclipseContext
	 * @param commandId
	 * @return
	 */
	public static StructuredSelection getCommandSelection(IEclipseContext iEclipseContext, String commandId) {
		return getCommandSelection(iEclipseContext, commandId, true);
	}

	/**
	 * Retrieve the selection for the commandId (added by
	 * {@link CoreUiUtil#addCommandContributions(IMenuManager, Object[], String)})
	 * from the {@link IEclipseContext}. If the named variable is removed is
	 * specified with the remove parameter.
	 *
	 * @param iEclipseContext
	 * @param commandId
	 * @param remove
	 * @return
	 */
	public static StructuredSelection getCommandSelection(IEclipseContext iEclipseContext, String commandId,
			boolean remove) {
		StructuredSelection selection = (StructuredSelection) iEclipseContext.get(commandId.concat(".selection")); //$NON-NLS-1$
		if (remove) {
			iEclipseContext.remove(commandId.concat(".selection")); //$NON-NLS-1$
		}
		return selection;
	}

	/**
	 * Set the selection for the commandId in the current {@link IEclipseContext}.
	 * Retrievable via
	 * {@link CoreUiUtil#getCommandSelection(IEclipseContext, String)}.
	 *
	 * @param commandId
	 * @param selection
	 */
	public static void setCommandSelection(String commandId, Object[] selection) {
		applicationContext.set(commandId.concat(".selection"), //$NON-NLS-1$
				new StructuredSelection(selection));
	}

	/**
	 * Set the selection for the commandId in the current {@link IEclipseContext}.
	 * Retrievable via
	 * {@link CoreUiUtil#getCommandSelection(IEclipseContext, String)}.
	 *
	 * @param commandId
	 * @param selection
	 */
	public static void setCommandSelection(String commandId, List<?> selection) {
		setCommandSelection(commandId, selection.toArray());
	}
	
	private static Control getAsControl(Object object) {
		if (object != null) {
			if (object instanceof Control) {
				return (Control) object;
			}
			if (object instanceof Viewer) {
				return ((Viewer) object).getControl();
			}
			try {
				if (object.getClass().getMethod("getViewerWidget", new Class[0]) != null) {
					Method method = object.getClass().getMethod("getViewerWidget", (Class[]) null);

					Object viewerWidget = method.invoke(object, (Object[]) null);
					if (viewerWidget instanceof StructuredViewer) {
						return ((StructuredViewer) viewerWidget).getControl();
					}
					if (viewerWidget instanceof Control) {
						return (Control) viewerWidget;
					}
				}
			} catch (Exception e) {
				LoggerFactory.getLogger(CoreUiUtil.class).warn("Error getting viewer widget of [" + object + "]", e);
			}
			LoggerFactory.getLogger(CoreUiUtil.class).warn("Can not get Control from [" + object + "]");
		}
		return null;
	}
	
	public static void runAsyncIfActive(Runnable runnable, Object object) {
		Control control = getAsControl(object);
		if (control != null) {
			Display.getDefault().asyncExec(() -> {
				if (isActiveControl(control)) {
					runnable.run();
				}
			});
		}
	}
	
	public static void runIfActive(Runnable runnable, Object object) {
		Control control = getAsControl(object);
		if (control != null) {
			Display.getDefault().syncExec(() -> {
				if (isActiveControl(control)) {
					runnable.run();
				}
			});
		}
	}


	/**
	 * Compare the provided objects checking if both, return 0, or o1 is null return
	 * -1, or o2 is null return 1, or both not null return
	 * {@link Integer#MAX_VALUE}.
	 * 
	 * @param o1
	 * @param o2
	 * @return
	 */
	public static int compareNullSafe(Object o1, Object o2) {
		return o1 == null ? (o2 == null ? 0 : -1) : (o2 == null ? 1 : Integer.MAX_VALUE);

	}
}
