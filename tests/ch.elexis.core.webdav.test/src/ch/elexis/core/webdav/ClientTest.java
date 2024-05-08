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
import org.slf4j.LoggerFactory;

import com.github.sardine.impl.SardineException;

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
				LoggerFactory.getLogger(getClass()).info("TEST delete " + f.toString());
				f.delete();
			} catch (IOException e) {
				fail(e.getMessage());
			}
		});
	}

	@Test
	public void connectAnon() throws MalformedURLException, IOException {
		URLConnection directory = new URL("dav://localhost:22808/remote.php/dav/files/admin").openConnection();
		assertTrue(directory instanceof WebdavFile);
		// access rights are checked on access
		boolean exception = false;
		try {
			Arrays.asList(((WebdavFile) directory).listFiles(null));
		} catch (Exception e) {
			assertTrue(e instanceof SardineException);
			assertEquals(401, ((SardineException) e).getStatusCode());
			exception = true;
		}
		assertTrue(exception);
	}

	@Test
	public void putSmallTxt() throws MalformedURLException, IOException, InterruptedException {

		URLConnection connection = new URL(
				"dav://admin:admin@localhost:22808/remote.php/dav/files/admin/webdavTest.txt")
				.openConnection();
		assertTrue(connection instanceof WebdavFile);

		byte[] randomBytes = UUID.randomUUID().toString().getBytes();

		LoggerFactory.getLogger(getClass()).info("TEST write start");
		try (OutputStream outputStream = connection.getOutputStream()) {
			IOUtils.write(randomBytes, outputStream);
		}
		((WebdavFile) connection).waitWriteComplete();
		LoggerFactory.getLogger(getClass()).info("TEST write complete");

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

		LoggerFactory.getLogger(getClass()).info("TEST write start");
		try (OutputStream outputStream = connection.getOutputStream()) {
			IOUtils.write(randomBytes, outputStream);
		}
		((WebdavFile) connection).waitWriteComplete();
		LoggerFactory.getLogger(getClass()).info("TEST write complete");

		assertEquals(randomBytes.length, connection.getContentLength());

		byte[] readByteArray;
		try (InputStream inputStream = connection.getInputStream()) {
			readByteArray = IOUtils.toByteArray(inputStream);
		}
		assertArrayEquals(randomBytes, readByteArray);

		((WebdavFile) connection).delete();
	}

	private byte[] testBytes;
	private URLConnection testConnection;

	@Test
	public void put10MbMultithread() throws MalformedURLException, IOException, InterruptedException {

		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			final int index = i;
			futures.add(CompletableFuture.runAsync(() -> {
				byte[] randomBytes = new byte[1000 * 1000 * 10];
				new Random().nextBytes(randomBytes);

				try {
					URLConnection connection;
					connection = new URL(
							"dav://admin:admin@localhost:22808/remote.php/dav/files/admin/webdavTest_" + index + ".bin")
							.openConnection();
					assertTrue(connection instanceof WebdavFile);
					LoggerFactory.getLogger(getClass()).info("TEST write start " + index);
					try (OutputStream outputStream = connection.getOutputStream()) {
						IOUtils.write(randomBytes, outputStream);
					}
					((WebdavFile) connection).waitWriteComplete();
					LoggerFactory.getLogger(getClass()).info("TEST write complete " + index);

					if (index == 7) {
						testBytes = randomBytes;
						testConnection = connection;
					}
				} catch (IOException e) {
					fail(e.getMessage());
				}
			}));
		}

		CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).join();

		byte[] readByteArray;
		try (InputStream inputStream = testConnection.getInputStream()) {
			readByteArray = IOUtils.toByteArray(inputStream);
		}
		assertArrayEquals(testBytes, readByteArray);

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
