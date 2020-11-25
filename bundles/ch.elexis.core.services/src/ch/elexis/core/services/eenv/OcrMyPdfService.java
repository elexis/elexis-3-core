package ch.elexis.core.services.eenv;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import com.eclipsesource.jaxrs.consumer.ConsumerFactory;
import com.eclipsesource.jaxrs.consumer.RequestException;

import ch.elexis.core.eenv.IElexisEnvironmentService;
import ch.elexis.core.eenv.IOcrMyPdfService;

@Component
public class OcrMyPdfService implements IOcrMyPdfService {
	
	@Reference
	private IElexisEnvironmentService elexisEnvironmentService;
	
	private IRemoteOcrMyPdfService remoteOcrMyPdfService;
	
	@Activate
	public void activate(){
		try {
			String ocrMyPdfUrl = elexisEnvironmentService.getOcrMyPdfBaseUrl();
			remoteOcrMyPdfService = ConsumerFactory.createConsumer(ocrMyPdfUrl,
				new OcrMyPdfClientConfig(), IRemoteOcrMyPdfService.class);
		} catch (Exception e) {
			LoggerFactory.getLogger(getClass()).warn("Error activating service", e);
		}
	}
	
	private final String PARAMS = "-l deu";
	
	@Override
	public byte[] performOcr(byte[] in, String parameters) throws IOException{
		if (in == null) {
			throw new IllegalArgumentException("null");
		}
		
		String params = (parameters != null) ? parameters : PARAMS;
		
		try (FormDataMultiPart form = new FormDataMultiPart()) {
			form.bodyPart(
				new StreamDataBodyPart("file", new ByteArrayInputStream(in), "filename.pdf"));
			form.field("params", params);
			InputStream performOcr = remoteOcrMyPdfService.performOcr(form);
			return IOUtils.toByteArray(performOcr);
		} catch (RequestException re) {
			if (re.getStatus() == 400 && re.getMessage().contains("already")) {
				return in;
			}
			throw new IllegalStateException("invalid state", re);
		}
		
	}
	
}
