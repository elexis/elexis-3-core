package ch.elexis.core.model;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;

import ch.elexis.core.test.AbstractTest;

public class BlobTest extends AbstractTest {

	@Test
	public void createDeleteHeap() throws IOException {
		IBlob blob = coreModelService.create(IBlob.class);
		blob.setId("testblob");
		coreModelService.save(blob);

		blob = coreModelService.load("testblob", IBlob.class).orElse(null);
		assertNotNull(blob);

		blob.setContent(new byte[] { 0x01, 0x02, 0x03 });
		coreModelService.save(blob);

		blob = coreModelService.load("testblob", IBlob.class).orElse(null);
		assertNotNull(blob);
		assertNotNull(blob.getContent());

		coreModelService.delete(blob);
	}
}
