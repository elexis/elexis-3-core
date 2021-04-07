package ch.elexis.core.services;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.services.internal.VirtualFilesystemHandle;

@Component
public class VirtualFilesystemService implements IVirtualFilesystemService {
	
	@Override
	public IVirtualFilesystemHandle of(String uriString) throws IOException{
		try {
			URI uri = IVirtualFilesystemService.stringToURI(uriString);
			return new VirtualFilesystemHandle(uri);
		} catch (MalformedURLException | URISyntaxException e) {
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
