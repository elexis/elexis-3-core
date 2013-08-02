package ch.elexis.core.logging;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import ch.elexis.core.logging.LoggingConstants;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ch.elexis.core.logging"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
		
	// logger name = "org.ekkehard.osgi.over.slf4j.Activator"
	private static final Logger logger = LoggerFactory.getLogger(Activator.class);
	
	// The Bundle Marker: a Marker where the name is the osgi bundle symbolic name
	// and an attached IS_BUNDLE - Marker to guarantee that the Log Framework knows its a
	// BundleMarker
	public static final Marker bundleMarker = createBundleMarker();
	private BundleContext context = null;

	private static final Marker createBundleMarker(){
		Marker bundleMarker = MarkerFactory.getMarker(PLUGIN_ID);
		bundleMarker.add(MarkerFactory.getMarker(LoggingConstants.IS_BUNDLE_MARKER));
		return bundleMarker;
	}
	
	/**
	 * The constructor
	 */
	public Activator() {
		this.context = context;
		
		// route java.util.logging to slf4j
/*
		SLF4JBridgeHandler.install();
		if (System.getProperty("osgi.console") == null && System.getProperty("eclipse.consoleLog") == null)
		{
//			SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();
//			SysOutOverSLF4J.sendSystemOutAndErrToSLF4J(LogLevel.DEBUG, LogLevel.WARN);
			System.out.println("This string is only here to check, whether System.out goes to the log, too"); //$NON-NLS-1$		
		} else {
			System.out.println("System.out not redirected to logging as -console or -consoleLog passed as argument"); //$NON-NLS-1$					
		}
		logger.info(PLUGIN_ID+": started SLF4JBridgeHandler");
		
		// print internal state
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		StatusPrinter.print(lc);
		logger.info(PLUGIN_ID+": printed internal state");
		
		// log BundleEvents
		context.addBundleListener(new BundleListener() {
			
			public void bundleChanged(BundleEvent event){
				
				String typeMessage = null;
				switch (event.getType()) {
				case BundleEvent.INSTALLED:
					typeMessage = "installed"; //$NON-NLS-1$
					break;
				case BundleEvent.LAZY_ACTIVATION:
					typeMessage = "lazy activation"; //$NON-NLS-1$
					break;
				case BundleEvent.RESOLVED:
					typeMessage = "resolved"; //$NON-NLS-1$
					break;
				case BundleEvent.STARTED:
					typeMessage = "started"; //$NON-NLS-1$
					break;
				case BundleEvent.STARTING:
					typeMessage = "starting"; //$NON-NLS-1$
					break;
				case BundleEvent.STOPPED:
					typeMessage = "stopped"; //$NON-NLS-1$
					break;
				case BundleEvent.STOPPING:
					typeMessage = "stopping"; //$NON-NLS-1$
					break;
				case BundleEvent.UNINSTALLED:
					typeMessage = "uninstalled"; //$NON-NLS-1$
					break;
				case BundleEvent.UNRESOLVED:
					typeMessage = "unresolved"; //$NON-NLS-1$
					break;
				case BundleEvent.UPDATED:
					typeMessage = "updated"; //$NON-NLS-1$
					break;
				default:
					typeMessage = "unknown bundle event: " + event.getType(); //$NON-NLS-1$
					break;
				}
				
				logger.info(bundleMarker, "BundleEvent: B: {} new state: {}", //$NON-NLS-1$
					event.getBundle().getSymbolicName(), typeMessage);
			}
		});
		
		// log FrameworkEvents
		context.addFrameworkListener(new FrameworkListener() {
			
			public void frameworkEvent(FrameworkEvent event){
				
				switch (event.getType()) {
				case FrameworkEvent.ERROR:
					logger.error(bundleMarker, "FrameworkEvent: ERROR in bundle {}", //$NON-NLS-1$
						event.getBundle().getSymbolicName(), event.getThrowable());
					break;
				case FrameworkEvent.WARNING:
					logger.warn(bundleMarker, "FrameworkEvent: WARNING in bundle {}", //$NON-NLS-1$
						event.getBundle().getSymbolicName());
					break;
				case FrameworkEvent.STARTED:
					logger.info(bundleMarker, "FrameworkEvent bundle {} started", //$NON-NLS-1$
						event.getBundle().getSymbolicName());
					break;
				default:
					// not logged:
					// FrameworkEvent.INFO,
					// FrameworkEvent.PACKAGES_REFRESHED,
					// FrameworkEvent.STARTLEVEL_CHANGED
					break;
				}
				
			}
			
		});
		*/
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
