package ch.elexis;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;

public class ResourceManager {
	
	private static ResourceManager instance;
	
	private ResourceManager(){
		
	}
	
	public static ResourceManager getInstance(){
		if (instance == null)
			return new ResourceManager();
		return instance;
	}
	
	public URI getResourceURIByName(String name){
		try {
			// try to load the resource directly
			URL url = getClass().getResource(name);
			if (url == null) {
				// we are probably in a test fragment respective bundle
				System.out.println(getClass().getResource("."));
				url = getClass().getResource("/rsc/" + name);
				url = FileLocator.toFileURL(url);
			}
			URI ret = url.toURI();
			return ret;
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getResourceLocationByName(String name){
		URI resourceURI = getResourceURIByName(name);
		if (resourceURI == null)
			return null;
		// remove file:/ header from string
		String uri = resourceURI.toString();
		int start = uri.indexOf('/');
		return uri.substring(start);
	}
}
