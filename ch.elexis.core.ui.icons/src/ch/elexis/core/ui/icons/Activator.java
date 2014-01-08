package ch.elexis.core.ui.icons;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import ch.elexis.core.ui.icons.urihandler.IconURLStreamHandlerService;

public class Activator implements BundleActivator {
	
	public static final String PLUGIN_ID = "ch.elexis.core.ui.icons";
	
	private static BundleContext context;
	
	static BundleContext getContext(){
		return context;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception{
		Activator.context = bundleContext;
		IconURLStreamHandlerService.getInstance().register();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception{
		Activator.context = null;
		IconURLStreamHandlerService.getInstance().unregister();
	}
	
}
