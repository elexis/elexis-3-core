package ch.elexis.core.services.eenv;

import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * @see https://github.com/jbarlow83/OCRmyPDF/blob/master/misc/webservice.py
 */
@Path(StringUtils.EMPTY)
interface IRemoteOcrMyPdfService {

	@POST
	@Path("execute")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("application/pdf")
	public InputStream performOcr(final FormDataMultiPart multiPart);

}
