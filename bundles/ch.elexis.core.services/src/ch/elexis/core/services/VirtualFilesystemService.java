package ch.elexis.core.services;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;

import ch.elexis.core.services.internal.VirtualFilesystemHandle;
import ch.elexis.core.utils.CoreUtil;

@Component
public class VirtualFilesystemService implements IVirtualFilesystemService {
	
	@Override
	public IVirtualFilesystemHandle of(String uriString) throws IOException{
		
		if (StringUtils.startsWith(uriString, "\\\\")) {
			String replaced = uriString.replace("\\", "/");
			if (CoreUtil.isWindows()) {
				// https://wiki.eclipse.org/Eclipse/UNC_Paths
				uriString = "file://" + replaced;
			} else {
				uriString = "smb:" + replaced;
			}
		}
		
		if (uriString.startsWith("/")) {
			uriString = "file:" + uriString;
		}
		
		URL url = new URL(uriString);
		// url may contain '#' characters which in a URL is referred to as a fragment (leading to a getRef())
		// We don't use it as those, that is we don't have fragments, so we need to pass
		// this to the path
		String path = url.getPath();
		if (url.getRef() != null) {
			path += "#" + url.getRef();
		}
		
		try {
			URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(),
				path, url.getQuery(), null);
			return new VirtualFilesystemHandle(uri);
		} catch (URISyntaxException e) {
			throw new IOException("Error handling uri string [" + uriString + "]", e);
		}
	}
	
	@Override
	public IVirtualFilesystemHandle of(File file) throws IOException{
		if (file == null) {
			return null;
		}
		
		URI uri = file.toURI();
		return new VirtualFilesystemHandle(uri);
	}
	
}
