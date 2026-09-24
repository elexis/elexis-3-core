package ch.elexis.core.services.rcp.eenv;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

class OcrMyPdfClientConfig extends ClientConfig {

	public OcrMyPdfClientConfig() {
		register(MultiPartFeature.class);
	}

}
