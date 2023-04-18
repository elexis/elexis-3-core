package ch.elexis.core.webdav.internal;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;

import ch.elexis.core.webdav.WebdavFile;

@Component(immediate = true, service = URLStreamHandlerService.class, property = {
		URLConstants.URL_HANDLER_PROTOCOL + ":String=dav" })
public class UnsafeWebdavURLStreamHandlerService extends AbstractURLStreamHandlerService {

	@Override
	public URLConnection openConnection(URL url) throws IOException {
		if (url.getUserInfo() != null && !"localhost".equals(url.getHost())) {
			throw new IOException("No unencrypted, authenticated communication to external hosts allowed. Use davs.");
		}
		String replaced = url.toString().replaceFirst("dav", "http");
		return new WebdavFile(new URL(replaced));
	}

}
