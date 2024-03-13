package ch.elexis.core.webdav;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

public class ClientTest {

	@Before
	public void before() throws MalformedURLException, IOException {
		URLConnection directory = new URL("dav://admin:admin@localhost:22808/remote.php/dav/files/admin")
				.openConnection();
		assertTrue(directory instanceof WebdavFile);

		List<WebdavFile> files = Arrays.asList(((WebdavFile) directory).listFiles(null)).stream().filter(f -> {
			try {
				return !f.isDirectory();
			} catch (IOException e) {
				fail(e.getMessage());
			}
			return true;
		}).collect(Collectors.toList());
		files.forEach(f -> {
			try {
				System.out.println("Delete " + f.toString());
				f.delete();
			} catch (IOException e) {
				fail(e.getMessage());
			}
		});
	}

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
		((WebdavFile) connection).waitWriteComplete();

		assertEquals(randomBytes.length, connection.getContentLength());

		byte[] readByteArray;
		try (InputStream inputStream = connection.getInputStream()) {
			readByteArray = IOUtils.toByteArray(inputStream);
		}
		assertArrayEquals(randomBytes, readByteArray);

		((WebdavFile) connection).delete();
	}

	@Test
	public void put10Mb() throws MalformedURLException, IOException, InterruptedException {

		byte[] randomBytes = new byte[1000 * 1000 * 10];
		new Random().nextBytes(randomBytes);

		URLConnection connection = new URL(
				"dav://admin:admin@localhost:22808/remote.php/dav/files/admin/webdavTest.bin").openConnection();
		assertTrue(connection instanceof WebdavFile);

		try (OutputStream outputStream = connection.getOutputStream()) {
			IOUtils.write(randomBytes, outputStream);
		}
		((WebdavFile) connection).waitWriteComplete();

		assertEquals(randomBytes.length, connection.getContentLength());

		byte[] readByteArray;
		try (InputStream inputStream = connection.getInputStream()) {
			readByteArray = IOUtils.toByteArray(inputStream);
		}
		assertArrayEquals(randomBytes, readByteArray);

		((WebdavFile) connection).delete();
	}

	@Test
	public void put10MbMultithread() throws MalformedURLException, IOException, InterruptedException {

		byte[] randomBytes = new byte[1000 * 1000 * 10];
		new Random().nextBytes(randomBytes);

		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			final int index = i;
			futures.add(CompletableFuture.runAsync(() -> {
				try {
					URLConnection connection;
					connection = new URL(
							"dav://admin:admin@localhost:22808/remote.php/dav/files/admin/webdavTest_" + index + ".bin")
							.openConnection();
					assertTrue(connection instanceof WebdavFile);

					try (OutputStream outputStream = connection.getOutputStream()) {
						IOUtils.write(randomBytes, outputStream);
					}
					((WebdavFile) connection).waitWriteComplete();
				} catch (IOException e) {
					fail(e.getMessage());
				}
			}));
		}

		CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).join();

		URLConnection directory = new URL("dav://admin:admin@localhost:22808/remote.php/dav/files/admin")
				.openConnection();
		assertTrue(directory instanceof WebdavFile);

		List<WebdavFile> files = Arrays.asList(((WebdavFile) directory).listFiles(null)).stream().filter(f -> {
			try {
				return !f.isDirectory();
			} catch (IOException e) {
				fail(e.getMessage());
			}
			return true;
		}).collect(Collectors.toList());
		assertEquals(10, files.size());
	}
}
