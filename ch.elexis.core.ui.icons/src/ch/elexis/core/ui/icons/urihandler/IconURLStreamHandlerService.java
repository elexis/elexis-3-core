package ch.elexis.core.ui.icons.urihandler;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, service = URLStreamHandlerService.class, property = {
	URLConstants.URL_HANDLER_PROTOCOL + ":String=icon"
})
public class IconURLStreamHandlerService extends AbstractURLStreamHandlerService {
	
	private Logger log = LoggerFactory.getLogger(IconURLStreamHandlerService.class);
	
	@Activate
	public void activate() {
		log.debug("Icon URL handler registered.");
	}
	
	@Override
	public URLConnection openConnection(URL u) throws IOException{
		return new IconURLConnection(u);
	}
	
}
