package ch.elexis.core.services.eenv;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ch.elexis.core.eenv.IElexisEnvironmentService;
import ch.elexis.core.eenv.IOcrMyPdfService;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;

@Component
public class OcrMyPdfService implements IOcrMyPdfService {

	@Reference
	private IElexisEnvironmentService elexisEnvironmentService;

	private IRemoteOcrMyPdfService remoteOcrMyPdfService;

	@Activate
	public void activate() {
		try {
			String ocrMyPdfUrl = elexisEnvironmentService.getOcrMyPdfBaseUrl();
			Client client = ClientBuilder.newBuilder().connectTimeout(5, TimeUnit.SECONDS)
					.register(MultiPartFeature.class).build();
			// FIXME test
			remoteOcrMyPdfService = WebResourceFactory.newResource(IRemoteOcrMyPdfService.class,
					client.target(ocrMyPdfUrl));
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

		final String params = (parameters != null) ? parameters : PARAMS;

		try (FormDataMultiPart form = new FormDataMultiPart()) {
			form.bodyPart(new StreamDataBodyPart("file", new ByteArrayInputStream(in), "filename.pdf"));
			form.field("params", params);
			InputStream performOcr = remoteOcrMyPdfService.performOcr(form);
			return IOUtils.toByteArray(performOcr);
		} catch (ClientErrorException re) {
			final int status = re.getResponse().getStatus();
			Object entity = re.getResponse().getEntity();
			String body = String.valueOf(entity);
			if (entity instanceof InputStream is) {
				body = IOUtils.toString(is, StandardCharsets.UTF_8);
			}
			if (status == 400) {
				if (body.contains("already")) {
					return in;
				} else if (body.contains("TaggedPDFError")) {
					// This PDF is marked as a Tagged PDF. This often indicates
					// that the PDF was generated from an office document and does
					// not need OCR.
					return in;
				} else if (body.contains("encrypted")) {
					throw new OcrMyPdfException(OcrMyPdfException.TYPE.ENCRYPTED_FILE);
				} else if (body.contains("dynamic XFA")) {
					throw new OcrMyPdfException(OcrMyPdfException.TYPE.UNREADABLE_XFA_FORM_FILE);
				}
				throw new OcrMyPdfException(OcrMyPdfException.TYPE.OTHER, re.getMessage() + " [" + body + "]");
			}
			if (status == 413) {
				throw new OcrMyPdfException(OcrMyPdfException.TYPE.OTHER, "(HTTP 413) PDF is too large.");
			}
			throw new IllegalStateException("invalid state " + status + ": " + re.getMessage(), re);
		}

	}

}
