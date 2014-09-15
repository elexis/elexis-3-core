package ch.elexis.core.ui.p2;

import org.eclipse.equinox.p2.ui.Policy;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import ch.elexis.core.ui.p2.policy.ElexisCloudPolicy;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
	
	// The plug-in ID
	public static final String PLUGIN_ID = "ch.elexis.core.ui.p2"; //$NON-NLS-1$
	
	// The shared instance
	private static Activator plugin;
	
	ServiceRegistration policyRegistration;
	ElexisCloudPolicy policy;
	IPropertyChangeListener preferenceListener;
	
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
		registerP2Policy(context);
		getPreferenceStore().addPropertyChangeListener(getPreferenceListener());
		
// ServiceReference<IProvisioningAgent> serviceReference =
// context.getServiceReference(IProvisioningAgent.class);
// IProvisioningAgent agent = context.getService(serviceReference);
// if (agent == null) {
// System.out.println(">> no agent loaded!");
// return;
// }
//
// if (! P2Util.addRepository(agent, "http://download.elexis.info/elexis.3.snapshot")) {
// System.out.println(">> could no add repostory!");
// return;
// }
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception{
		plugin = null;
		policyRegistration.unregister();
		policyRegistration = null;
		getPreferenceStore().removePropertyChangeListener(preferenceListener);
		preferenceListener = null;
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
	
	private void registerP2Policy(BundleContext context){
		policy = new ElexisCloudPolicy();
		policy.updateForPreferences();
		policyRegistration = context.registerService(Policy.class.getName(), policy, null);
	}
	
	private IPropertyChangeListener getPreferenceListener(){
		if (preferenceListener == null) {
			preferenceListener = new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event){
					policy.updateForPreferences();
				}
			};
		}
		return preferenceListener;
	}
}
