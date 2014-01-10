package ch.elexis.core.ui.contacts;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ch.elexis.core.ui.contacts.extension.ContactGeonamesExtensionPoint;
import ch.elexis.data.Anwender;
import ch.elexis.data.Patient;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
	
	// The plug-in ID
	public static final String PLUGIN_ID = "ch.elexis.core.ui.contacts"; //$NON-NLS-1$
	
	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator(){}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception{
		super.start(context);
		plugin = this;
		
		// we need to initialize the static header
		// in order to have the correct PersistentObject mappings
		Class.forName(Patient.class.getName());
		Class.forName(Anwender.class.getName());
		
		// Initialize geonames content proposal
		ContactGeonamesExtensionPoint.init();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception{
		plugin = null;
		super.stop(context);
	}
	
	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault(){
		return plugin;
	}
	
}
