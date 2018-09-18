package ch.elexis.core.model;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import ch.elexis.core.services.IModelService;
import ch.elexis.core.utils.OsgiServiceUtil;

public class DbImageTest {
	
	private IModelService modelService;
	
	public DbImageTest(){
		modelService = OsgiServiceUtil.getService(IModelService.class).get();
	}
	
	@Test
	public void createDeleteDbImage() throws IOException{
		IImage image = modelService.create(IImage.class);
		image.setDate(LocalDate.now());
		image.setTitle("RandomImage." + MimeType.png.name());
		try (InputStream is = getClass().getClassLoader().getResourceAsStream("./elexis.png")) {
			byte[] byteArray = IOUtils.toByteArray(is);
			image.setImage(byteArray);

		}
		modelService.save(image);
		modelService.delete(image);
	}
	
}
