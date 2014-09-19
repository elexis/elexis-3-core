package ch.elexis.core.scheduler.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.scheduler.ElexisScheduler;

public class Activator implements BundleActivator {
	
	public static final String PLUGIN_ID = "ch.elexis.core.scheduler";
	
	private static BundleContext context;
	private static Logger log = LoggerFactory.getLogger(Activator.class);
	
	private String connPref = null;
	
	@Override
	public void start(BundleContext context) throws Exception{
		Activator.context = context;
		
		// only start if we have a db connection configured
		connPref = CoreHub.localCfg.get(Preferences.CFG_FOLDED_CONNECTION, null);
		if (connPref != null) {
			ElexisScheduler.getInstance().startScheduler();
		} else {
			log.error("No database configuration found, stopping startup");
			stop(context);
		}
	}
	
	@Override
	public void stop(BundleContext context) throws Exception{
		if (connPref != null) {
			ElexisScheduler.getInstance().shutdownScheduler();
		}
		Activator.context = null;
	}
	
	/**
	 * @return the <code>quartz.properties</code> file as {@link Properties} object
	 */
	public static Properties getQuartzProperties(){
		Properties properties = new Properties();
		try {
			URL resource = context.getBundle().getEntry("quartz.properties");
			InputStream openStream = resource.openStream();
			properties.load(openStream);
			openStream.close();
		} catch (IOException e) {
			log.error("Error loading properties", e);
		}
		return properties;
	}
	
	public static URL getResource(String resource){
		return context.getBundle().getEntry(resource);
	}
	
}
