package ch.elexis.core.ui.icons.urihandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ui.icons.ImageSize;
import ch.elexis.core.ui.icons.Images;

public class IconURLConnection extends URLConnection {
	
	private static Logger log = LoggerFactory.getLogger(IconURLConnection.class);
	
	String iconName;
	
	protected IconURLConnection(URL url){
		super(url);
		iconName = url.getAuthority();
	}
	
	@Override
	public InputStream getInputStream() throws IOException{
		try {
			Images selectedIcon = Images.valueOf(iconName);
			return selectedIcon.getImageAsInputStream(ImageSize._16x16_DefaultIconSize);
		} catch (IllegalArgumentException e) {
			log.error("[ERROR] " + iconName + " not found, replacing with empty icon.");
			return Images.IMG_CLEAR.getImageAsInputStream(ImageSize._16x16_DefaultIconSize);
		}
	}
	
	@Override
	public void connect() throws IOException{}
}
