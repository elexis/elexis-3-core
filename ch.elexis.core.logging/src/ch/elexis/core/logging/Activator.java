package ch.elexis.core.logging;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import uk.org.lidalia.sysoutslf4j.context.LogLevel;
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private static  Logger logger = null;
	private static String PLUGIN_ID = "ch.elexis.core.logging";
	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		if (logger == null)
			logger = LoggerFactory.getLogger(Activator.class);
			
		// route java.util.logging to slf4j
		SLF4JBridgeHandler.install();
		if (System.getProperty("osgi.console") == null && System.getProperty("eclipse.consoleLog") == null)
		{
			SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();
			SysOutOverSLF4J.sendSystemOutAndErrToSLF4J(LogLevel.DEBUG, LogLevel.WARN);
			System.out.println("This string is only here to check, whether System.out goes to the log, too"); //$NON-NLS-1$		
		} else {
			System.out.println("System.out not redirected to logging as -console or -consoleLog passed as argument"); //$NON-NLS-1$					
		}
		logger.info(PLUGIN_ID+": started SLF4JBridgeHandler");
		// print internal state
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		StatusPrinter.print(lc);
		logger.info(PLUGIN_ID+": printed internal state");
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}
