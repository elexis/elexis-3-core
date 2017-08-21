package ch.elexis.core.logging;

import java.io.IOException;
import java.nio.file.Paths;

import org.eclipse.osgi.service.datalocation.Location;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import uk.org.lidalia.sysoutslf4j.context.LogLevel;
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

public class Activator implements BundleActivator {
	private static Logger logger = null;
	private static String PLUGIN_ID = "ch.elexis.core.logging";
	private static BundleContext myContext;
	
	@Override
	public void start(BundleContext bundleContext) throws Exception{
		myContext = bundleContext;
		configureLogbackInBundle(bundleContext.getBundle());
	}
	
	@Override
	public void stop(BundleContext bundleContext) throws Exception{}
	
	private void configureLogbackInBundle(Bundle bundle) throws JoranException, IOException{
		if (logger == null) {
			logger = LoggerFactory.getLogger(Activator.class);
		}
		String path = null;
		JoranConfigurator jc = new JoranConfigurator();
		
		// get the configuration location where the logback.xml is located
		Filter filter;
		try {
			filter = myContext.createFilter(Location.INSTALL_FILTER);
			ServiceTracker installLocation = new ServiceTracker(myContext, filter, null);
			installLocation.open();
			Location cfgLoc = (Location) installLocation.getService();
			path = cfgLoc.getURL().getPath()  + "logback.xml";
			if (Paths.get(path).toFile().canRead()) {
				jc.doConfigure(path);
			} else {
				logger.warn("Could not read config from " + path);
				path = null;
			}
		} catch (Exception ex) {
			ex.printStackTrace(System.out);
		}
		// route java.util.logging to slf4j
		SLF4JBridgeHandler.install();
		SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();
		SysOutOverSLF4J.sendSystemOutAndErrToSLF4J(LogLevel.DEBUG, LogLevel.WARN);
		String msg = "This string is only here to check, whether System.out goes to the log, too"; //$NON-NLS-1$
		System.out.println(msg);
		logger.info(PLUGIN_ID + ": started SLF4JBridgeHandler");
		// print internal state
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		StatusPrinter.print(lc);
		logger.info(PLUGIN_ID + ": printed internal state as specified in " + (path == null ? "default" : path));
	}
}
