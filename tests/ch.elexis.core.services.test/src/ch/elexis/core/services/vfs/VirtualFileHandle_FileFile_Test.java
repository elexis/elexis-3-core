package ch.elexis.core.services.vfs;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.utils.OsgiServiceUtil;

public class VirtualFileHandle_FileFile_Test extends AbstractVirtualFileHandleTest {

	static File testDirectory;
	static File testFile;

	@BeforeClass
	public static void beforeClass() throws IOException {
		service = OsgiServiceUtil.getService(IVirtualFilesystemService.class).get();
		Path tempDirectory = Files.createTempDirectory("virtualFilesystemTest_filefile");
		testDirectory = tempDirectory.toFile();
		testDirectory.deleteOnExit();
		testFile = Files.createTempFile(tempDirectory, "test", ".txt").toFile();
		testFile.deleteOnExit();
		createPopulateTestFile();
		testHandle = service.of(testFile);
	}
	
	private static void createPopulateTestFile() throws IOException {
		Files.write(testFile.toPath(), "meaninglessTestText".getBytes(), StandardOpenOption.CREATE);
	}

	@Test
	public void testOpenOutputStream() throws IOException {
		try (OutputStream os = testHandle.openOutputStream()) {
			IOUtils.write(LocalDateTime.now().toString(), os, Charset.defaultCharset());
		}
	}

	@Test
	public void testCopyTo() throws IOException {
		File copyToFile = new File(testDirectory, "copyToFile");
		testHandle.copyTo(service.of(copyToFile));
		byte[] readAllBytes = Files.readAllBytes(copyToFile.toPath());
		assertArrayEquals("meaninglessTestText".getBytes(), readAllBytes);
		assertTrue(testHandle.exists());
		assertTrue(copyToFile.delete());
	}

	@Test
	public void testGetParent() throws IOException {
		IVirtualFilesystemHandle parent = testHandle.getParent();
		assertEquals(testDirectory, parent.toFile().get());
	}

	@Test(expected = IOException.class)
	public void testListHandles() throws IOException {
		testHandle.listHandles();
	}

	@Test(expected = IOException.class)
	public void testListHandlesIVirtualFilesystemhandleFilter() throws IOException {
		testHandle.listHandles(null);
	}

	@Test
	public void testDelete() throws IOException {
		testHandle.delete();
		assertFalse(testFile.exists());
		assertTrue(testFile.createNewFile());
		createPopulateTestFile();
	}

	@Test
	public void testToURL() throws MalformedURLException {
		assertEquals(testFile.toURI().toURL(), testHandle.toURL());
	}

	@Test
	public void testIsDirectory() throws IOException {
		assertFalse(testHandle.isDirectory());
	}

	@Test
	public void testToFile() {
		assertEquals(testFile, testHandle.toFile().get());
	}

	@Test
	public void testGetExtension() {
		assertEquals("txt", testHandle.getExtension());
	}

	@Test
	public void testExists() throws IOException {
		assertTrue(testFile.delete());
		assertFalse(testHandle.exists());
		createPopulateTestFile();
		assertTrue(testHandle.exists());
	}

	@Test
	public void testGetName() {
		assertEquals(testFile.getName(), testHandle.getName());
	}

	@Test
	public void testCanRead() {
		assertTrue(testHandle.canRead());
	}

	@Test
	public void testGetAbsolutePath() {
		assertEquals(testFile.toURI().toString(), testHandle.getAbsolutePath());
	}

	@Test
	public void testMoveTo() throws IOException {
		File moveToFile = new File(testDirectory, "moveToFile");
		testHandle.moveTo(service.of(moveToFile));
		byte[] readAllBytes = Files.readAllBytes(moveToFile.toPath());
		assertArrayEquals("meaninglessTestText".getBytes(), readAllBytes);
		assertFalse(testHandle.exists());
		createPopulateTestFile();
	}

	@Test(expected = IOException.class)
	public void testSubDir() throws IOException {
		testHandle.subDir("subdir");
	}

	@Test(expected = IOException.class)
	public void testSubFile() throws IOException {
		testHandle.subFile("subfile");
	}

	public void testMkdir() throws IOException {
		// TODO behavior?
		testHandle.mkdir();
	}

}
