package ch.elexis.core.jcifs;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;

import jcifs.CIFSContext;
import jcifs.context.SingletonContext;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

@Component(immediate = true, service = URLStreamHandlerService.class, property = {
	URLConstants.URL_HANDLER_PROTOCOL + ":String=smb"
})
public class SmbURLStreamHandlerService extends AbstractURLStreamHandlerService {
	
	@Override
	public URLConnection openConnection(URL url) throws IOException{
		SingletonContext context = SingletonContext.getInstance();
		NtlmPasswordAuthentication ntlmPasswordAuthentication =
			new NtlmPasswordAuthentication(context, url.getUserInfo());
		CIFSContext credentials =
			SingletonContext.getInstance().withCredentials(ntlmPasswordAuthentication);
		return new SmbFile(url, credentials);
		
	}
	
}
