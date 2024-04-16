package ch.elexis.core.services;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ch.elexis.core.eenv.AccessToken;
import ch.elexis.core.services.internal.VirtualFilesystemHandle;

@Component
public class VirtualFilesystemService implements IVirtualFilesystemService {

	@Reference
	private IContextService contextService;

	@Override
	public IVirtualFilesystemHandle of(String uriString, boolean performVariableReplacement) throws IOException {
		try {
			String _uriString = uriString;
			if (performVariableReplacement) {
				_uriString = performVariableReplacement(_uriString);
			}
			URI uri = IVirtualFilesystemService.stringToURI(_uriString);
			return new VirtualFilesystemHandle(uri);
		} catch (MalformedURLException | URISyntaxException e) {
			throw new IOException("Error handling uri string [" + uriString + "]", e);
		}
	}

	@Override
	public IVirtualFilesystemHandle of(File file) throws IOException {
		if (file == null) {
			return null;
		}

		URI uri = file.toURI();
		return new VirtualFilesystemHandle(uri);
	}

	private String performVariableReplacement(String uriString) {
		if (uriString.startsWith("davs")) {
			uriString = uriString.replace("%7B", "{").replace("%7D", "}");
			if (uriString.contains("{ctx")) {
				AccessToken accessToken = contextService.getTyped(AccessToken.class).orElse(null);
				if (accessToken != null) {
					uriString = uriString.replace("{ctx.access-token}", accessToken.getToken())
							.replace("{ctx.preferred-username}", accessToken.getUsername());
				} else {
					LoggerFactory.getLogger(getClass())
							.warn("No access-token for replacement in url [{}] found, or no davs url", uriString);
				}
			}

		}
		return uriString;
	}

}
