package ch.elexis.core.services.eenv;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

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
