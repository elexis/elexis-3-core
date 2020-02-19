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
		testDirectoryHandle = service.of(testFile);
	}
	
	private static void createPopulateTestFile() throws IOException {
		Files.write(testFile.toPath(), "meaninglessTestText".getBytes(), StandardOpenOption.CREATE);
	}

	@Test
	public void testOpenOutputStream() throws IOException {
		try (OutputStream os = testDirectoryHandle.openOutputStream()) {
			IOUtils.write(LocalDateTime.now().toString(), os, Charset.defaultCharset());
		}
	}

	@Test
	public void testCopyTo() throws IOException {
		File copyToFile = new File(testDirectory, "copyToFile");
		testDirectoryHandle.copyTo(service.of(copyToFile));
		byte[] readAllBytes = Files.readAllBytes(copyToFile.toPath());
		assertArrayEquals("meaninglessTestText".getBytes(), readAllBytes);
		assertTrue(testDirectoryHandle.exists());
		assertTrue(copyToFile.delete());
	}

	@Test
	public void testGetParent() throws IOException {
		IVirtualFilesystemHandle parent = testDirectoryHandle.getParent();
		assertEquals(testDirectory, parent.toFile().get());
		assertTrue(testDirectoryHandle.getAbsolutePath()+" ["+parent.getName()+"]", parent.getName().startsWith("virtualFilesystemTest_filefile"));
	}

	@Test(expected = IOException.class)
	public void testListHandles() throws IOException {
		testDirectoryHandle.listHandles();
	}

	@Test(expected = IOException.class)
	public void testListHandlesIVirtualFilesystemhandleFilter() throws IOException {
		testDirectoryHandle.listHandles(null);
	}

	@Test
	public void testDelete() throws IOException {
		testDirectoryHandle.delete();
		assertFalse(testFile.exists());
		assertTrue(testFile.createNewFile());
		createPopulateTestFile();
	}

	@Test
	public void testToURL() throws MalformedURLException {
		assertEquals(testFile.toURI().toURL(), testDirectoryHandle.toURL());
	}

	@Test
	public void testIsDirectory() throws IOException {
		assertFalse(testDirectoryHandle.isDirectory());
	}

	@Test
	public void testToFile() {
		assertEquals(testFile, testDirectoryHandle.toFile().get());
	}

	@Test
	public void testGetExtension() {
		assertEquals("txt", testDirectoryHandle.getExtension());
	}

	@Test
	public void testExists() throws IOException {
		assertTrue(testFile.delete());
		assertFalse(testDirectoryHandle.exists());
		createPopulateTestFile();
		assertTrue(testDirectoryHandle.exists());
	}

	@Test
	public void testGetName() {
		assertEquals(testFile.getName(), testDirectoryHandle.getName());
	}

	@Test
	public void testCanRead() {
		assertTrue(testDirectoryHandle.canRead());
	}

	@Test
	public void testGetAbsolutePath() {
		assertEquals(testFile.toURI().toString(), testDirectoryHandle.getAbsolutePath());
	}

	@Test
	public void testMoveTo() throws IOException {
		File moveToFile = new File(testDirectory, "moveToFile");
		testDirectoryHandle.moveTo(service.of(moveToFile));
		byte[] readAllBytes = Files.readAllBytes(moveToFile.toPath());
		assertArrayEquals("meaninglessTestText".getBytes(), readAllBytes);
		assertFalse(testDirectoryHandle.exists());
		createPopulateTestFile();
	}

	@Test(expected = IOException.class)
	public void testSubDir() throws IOException {
		testDirectoryHandle.subDir("subdir");
	}

	@Test(expected = IOException.class)
	public void testSubFile() throws IOException {
		testDirectoryHandle.subFile("subfile");
	}

	public void testMkdir() throws IOException {
		// TODO behavior?
		testDirectoryHandle.mkdir();
	}

}
