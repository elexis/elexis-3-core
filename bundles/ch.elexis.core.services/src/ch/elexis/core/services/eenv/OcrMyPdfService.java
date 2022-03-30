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
	public void activate() {
		try {
			String ocrMyPdfUrl = elexisEnvironmentService.getOcrMyPdfBaseUrl();
			remoteOcrMyPdfService = ConsumerFactory.createConsumer(ocrMyPdfUrl, new OcrMyPdfClientConfig(),
					IRemoteOcrMyPdfService.class);
		} catch (Exception e) {
			LoggerFactory.getLogger(getClass()).warn("Error activating service", e);
		}
	}

	private final String PARAMS = "-l deu";

	/**
	 * The OCRMyPdf service we use currently accepts one request only. We can assert
	 * this for the given Elexis instance by using synchronized.
	 * 
	 * @throws OcrMyPdfException
	 * 
	 * @see https://ocrmypdf.readthedocs.io/en/latest/docker.html#using-the-ocrmypdf-web-service-wrapper
	 */
	@Override
	public synchronized byte[] performOcr(byte[] in, String parameters) throws IOException, OcrMyPdfException {
		if (in == null) {
			throw new IllegalArgumentException("null");
		}

		String params = (parameters != null) ? parameters : PARAMS;

		try (FormDataMultiPart form = new FormDataMultiPart()) {
			form.bodyPart(new StreamDataBodyPart("file", new ByteArrayInputStream(in), "filename.pdf"));
			form.field("params", params);
			InputStream performOcr = remoteOcrMyPdfService.performOcr(form);
			return IOUtils.toByteArray(performOcr);
		} catch (RequestException re) {
			if (re.getStatus() == 400 && re.getMessage().contains("already")) {
				return in;
			} else if (re.getStatus() == 400 && re.getMessage().contains("encrypted")) {
				throw new OcrMyPdfException(OcrMyPdfException.TYPE.ENCRYPTED_FILE);
			} else if (re.getStatus() == 400 && re.getMessage().contains("dynamic XFA")) {
				throw new OcrMyPdfException(OcrMyPdfException.TYPE.UNREADABLE_XFA_FORM_FILE);
			} else if (re.getStatus() == 400) {
				throw new OcrMyPdfException(OcrMyPdfException.TYPE.OTHER, re.getMessage());
			} else if (re.getStatus() == 413) {
				throw new OcrMyPdfException(OcrMyPdfException.TYPE.OTHER, "(HTTP 413) PDF is too large.");
			}
			throw new IllegalStateException("invalid state " + re.getStatus() + ": " + re.getMessage(), re);
		}

	}

}
