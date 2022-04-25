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

	private String iconName;

	private String iconPath;

	private int zoom;

	protected IconURLConnection(URL url) {
		super(url);
		iconName = url.getAuthority();
		zoom = 100;
		iconPath = url.getPath();
		if (iconPath != null && !iconPath.isEmpty()) {
			zoom = getZoom();
		}
	}

	private int getZoom() {
		if (iconPath.toLowerCase().endsWith("@2x.png")) {
			return 200;
		} else if (iconPath.toLowerCase().endsWith("@1.5x.png")) {
			return 150;
		}
		return 100;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		try {
			Images selectedIcon = Images.valueOf(iconName);
			if (zoom == 100) {
				return selectedIcon.getImageAsInputStream(ImageSize._16x16_DefaultIconSize);
			} else {
				return selectedIcon.getImageAsInputStream(ImageSize._16x16_DefaultIconSize, zoom);
			}
		} catch (IllegalArgumentException e) {
			log.error("[ERROR] " + iconName + " not found, replacing with empty icon.");
			return Images.IMG_CLEAR.getImageAsInputStream(ImageSize._16x16_DefaultIconSize);
		}
	}

	@Override
	public void connect() throws IOException {
	}
}
