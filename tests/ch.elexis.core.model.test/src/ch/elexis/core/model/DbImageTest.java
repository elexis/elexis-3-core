package ch.elexis.core.model;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import ch.elexis.core.test.AbstractTest;

public class DbImageTest extends AbstractTest {
	
	@Test
	public void createDeleteDbImage() throws IOException{
		IImage image = coreModelService.create(IImage.class);
		image.setDate(LocalDate.now());
		image.setTitle("RandomImage." + MimeType.png.name());
		try (InputStream is = getClass().getClassLoader().getResourceAsStream("./elexis.png")) {
			byte[] byteArray = IOUtils.toByteArray(is);
			image.setImage(byteArray);
			
		}
		coreModelService.save(image);
		coreModelService.delete(image);
	}
	
}
