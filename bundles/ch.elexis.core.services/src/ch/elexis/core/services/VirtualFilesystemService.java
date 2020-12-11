package ch.elexis.core.services;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.InvalidPathException;

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
		
		uriString = uriString.replaceAll(" ", "%20");
		
		URI uri = assertUri(uriString);
		return new VirtualFilesystemHandle(uri);
	}
	
	@Override
	public IVirtualFilesystemHandle of(File file) throws IOException{
		if (file == null) {
			return null;
		}
		
		URI uri = file.toURI();
		return new VirtualFilesystemHandle(uri);
	}
	
	private URI assertUri(String uriString) throws IOException{
		if (StringUtils.isBlank(uriString)) {
			throw new IOException("urlString is null");
		}
		
		URI uri = isValidURI(uriString);
		if (uri != null) {
			return uri;
		} else {
			File file = new File(uriString);
			try {
				file.toPath();
				return file.toURI();
			} catch (InvalidPathException e) {}
			
		}
		
		throw new IOException("Can not handle uri string [" + uriString + "]");
	}
	
	private URI isValidURI(String uriString){
		try {
			URI url = new URI(uriString);
			return url;
		} catch (Exception exception) {
			return null;
		}
	}
	
}
