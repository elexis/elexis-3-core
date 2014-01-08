package ch.elexis.core.ui.icons.urihandler;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;

import javax.swing.Icon;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO logging
public class IconURLStreamHandlerService extends AbstractURLStreamHandlerService {
	
	private static Logger log = LoggerFactory.getLogger(IconURLStreamHandlerService.class);
	
	private static IconURLStreamHandlerService instance;
	private ServiceRegistration<URLStreamHandlerService> iconUrlHandler;
	
	private IconURLStreamHandlerService(){}
	
	public static IconURLStreamHandlerService getInstance(){
		if (null == instance) {
			instance = new IconURLStreamHandlerService();
		}
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	public void register(){
		Bundle bundle = FrameworkUtil.getBundle(IconURLStreamHandlerService.class);
		BundleContext bundleContext = bundle.getBundleContext();
		try {
			@SuppressWarnings("rawtypes")
			Hashtable properties = new Hashtable();
			properties.put(URLConstants.URL_HANDLER_PROTOCOL, new String[] {
				"icon"
			});
			iconUrlHandler =
				bundleContext.registerService(URLStreamHandlerService.class, this, properties);
		} catch (Exception e) {
			log.error("Could not register icon URL handler.", e);
		}
		log.info("Icon URL handler registered.");
	}
	
	public void unregister(){
		try {
			if (iconUrlHandler != null) {
				iconUrlHandler.unregister();
				iconUrlHandler = null;
			}
		} catch (Exception e) {
			log.error("Could not register icon URL handler.", e);
			e.printStackTrace();
		}
	}
	
	@Override
	public URLConnection openConnection(URL u) throws IOException{
		return new IconURLConnection(u);
	}
	
}
