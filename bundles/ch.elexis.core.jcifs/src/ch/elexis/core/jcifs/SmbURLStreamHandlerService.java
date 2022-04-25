package ch.elexis.core.jcifs;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Properties;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jcifs.CIFSContext;
import jcifs.CIFSException;
import jcifs.context.SingletonContext;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

@Component(immediate = true, service = URLStreamHandlerService.class, property = {
		URLConstants.URL_HANDLER_PROTOCOL + ":String=smb" })
public class SmbURLStreamHandlerService extends AbstractURLStreamHandlerService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	static {
		try {
			// provide system properties as described in
			// https://github.com/AgNO3/jcifs-ng/blob/master/src/main/java/jcifs/config/PropertyConfiguration.java
			Properties properties = new Properties();
			if (System.getProperty("jcifs.smb.client.responseTimeout") == null) {
				properties.setProperty("jcifs.smb.client.responseTimeout", "15000");
			}
			SingletonContext.init(properties);
		} catch (CIFSException e) {
			e.printStackTrace();
		}
	}

	@Override
	public URLConnection openConnection(URL url) throws IOException {
		SingletonContext context = SingletonContext.getInstance();
		NtlmPasswordAuthentication ntlmPasswordAuthentication = new NtlmPasswordAuthentication(context,
				url.getUserInfo());
		CIFSContext credentials = SingletonContext.getInstance().withCredentials(ntlmPasswordAuthentication);
		// https://github.com/AgNO3/jcifs-ng/issues/271
		String _url = replaceEach(url.toExternalForm());
		logger.debug("openConnection [{}] [{}]", url, _url);
		return new SmbFile(_url, credentials);

	}

	private String replaceEach(String externalForm) throws UnsupportedEncodingException {
		externalForm = externalForm.replaceAll("%25", "%");
		return URLDecoder.decode(externalForm, "UTF-8");
	}

}
