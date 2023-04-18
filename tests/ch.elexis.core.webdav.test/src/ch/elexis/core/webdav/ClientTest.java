package ch.elexis.core.webdav;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class ClientTest {

	@Test
	public void createConnection() throws MalformedURLException, IOException, InterruptedException {

		URLConnection connection = new URL(
				"dav://admin:admin@localhost:22808/remote.php/dav/files/admin/webdavTest.txt")
				.openConnection();
		assertTrue(connection instanceof WebdavFile);

		byte[] randomBytes = UUID.randomUUID().toString().getBytes();

		try (OutputStream outputStream = connection.getOutputStream()) {
			IOUtils.write(randomBytes, outputStream);
		}

		Thread.sleep(500); // wait for pipe thread

		assertEquals(randomBytes.length, connection.getContentLength());

		byte[] readByteArray;
		try (InputStream inputStream = connection.getInputStream()) {
			readByteArray = IOUtils.toByteArray(inputStream);
		}
		assertArrayEquals(randomBytes, readByteArray);
	}

}
