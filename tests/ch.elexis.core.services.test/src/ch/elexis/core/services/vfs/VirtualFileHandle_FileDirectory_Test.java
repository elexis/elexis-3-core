package ch.elexis.core.services.vfs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.utils.OsgiServiceUtil;

public class VirtualFileHandle_FileDirectory_Test extends AbstractVirtualFileHandleTest {

	static Path tempDirectory;
	static File testDirectory;
	static File testSubDirectory;

	// TODO change to directory

	@BeforeClass
	public static void beforeClass() throws IOException {
		service = OsgiServiceUtil.getService(IVirtualFilesystemService.class).get();
		tempDirectory = Files.createTempDirectory("virtualFilesystemTest_filefile");
		testDirectory = new File(tempDirectory.toFile(), "subDir/");
		assertTrue(testDirectory.mkdir());
		testDirectory.deleteOnExit();
		testHandle = service.of(testDirectory);
	}

	@Test(expected = IOException.class)
	public void testOpenOutputStream() throws IOException {
		testHandle.openOutputStream();
	}

	@Test
	@Ignore
	public void testCopyTo() throws IOException {
//		File copyToFile = new File(testDirectory, "copyToFile");
//		testHandle.copyTo(service.of(copyToFile));
//		byte[] readAllBytes = Files.readAllBytes(copyToFile.toPath());
//		assertArrayEquals("meaninglessTestText".getBytes(), readAllBytes);
//		assertTrue(testHandle.exists());
		fail();
	}

	@Test
	public void testGetParent() throws IOException {
		IVirtualFilesystemHandle parent = testHandle.getParent();
		assertEquals(tempDirectory.toFile(), parent.toFile().get());
	}

	@Test
	public void testListHandles() throws IOException {
		File file = new File(testHandle.toFile().get(), "listingFile.txt");
		assertTrue(file.createNewFile());
		IVirtualFilesystemHandle[] listHandles = testHandle.listHandles();
		assertEquals(1, listHandles.length);
		assertEquals(file, listHandles[0].toFile().get());
		assertTrue(file.delete());
	}

	@Test
	public void testListHandlesIVirtualFilesystemhandleFilter() throws IOException {
		File file = new File(testHandle.toFile().get(), "listingFile.txt");
		assertTrue(file.createNewFile());
		File file2 = new File(testHandle.toFile().get(), "listingFile.txta");
		assertTrue(file2.createNewFile());
		IVirtualFilesystemHandle[] listHandles = testHandle
				.listHandles(handle -> "txt".equalsIgnoreCase(handle.getExtension()));
		assertEquals(1, listHandles.length);
		assertEquals(file, listHandles[0].toFile().get());
		assertTrue(file.delete());
		assertTrue(file2.delete());
	}

	@Test
	public void testDelete() throws IOException {
		testHandle.delete();
		assertFalse(testDirectory.exists());
		assertTrue(testDirectory.mkdir());
	}

	@Test
	public void testToURL() throws MalformedURLException {
		assertEquals(testDirectory.toURI().toURL(), testHandle.toURL());
	}

	@Test
	public void testIsDirectory() throws IOException {
		assertTrue(testHandle.isDirectory());
	}

	@Test
	public void testToFile() {
		assertEquals(testDirectory, testHandle.toFile().get());
	}

	@Test
	public void testGetExtension() {
		assertEquals("", testHandle.getExtension());
	}

	@Test
	public void testExists() throws IOException {
		assertTrue(testDirectory.delete());
		assertFalse(testHandle.exists());
		assertTrue(testDirectory.mkdir());
		assertTrue(testHandle.exists());
	}

	@Test
	public void testGetName() {
		assertEquals("", testHandle.getName());
	}

	@Test
	public void testCanRead() {
		assertTrue(testHandle.canRead());
	}

	@Test
	public void testGetAbsolutePath() {
		assertEquals(testDirectory.toURI().toString(), testHandle.getAbsolutePath());
	}

	@Test
	@Ignore
	public void testMoveTo() throws IOException {
//		File copyToFile = new File(testDirectory, "copyToFile");
//		assertTrue(testHandle.moveTo(service.of(copyToFile)));
//		byte[] readAllBytes = Files.readAllBytes(copyToFile.toPath());
//		assertArrayEquals("meaninglessTestText".getBytes(), readAllBytes);
//		assertFalse(testHandle.exists());
	}

	@Test
	public void testSubDir() throws IOException {
		IVirtualFilesystemHandle subDir = testHandle.subDir("subdir");
		assertFalse(subDir.exists());
		subDir.mkdir();
		assertTrue(subDir.exists());
	}

	@Test
	public void testSubFile() throws IOException {
		IVirtualFilesystemHandle subFile = testHandle.subFile("subfile");
		assertFalse(subFile.exists());
	}
	
	@Test(expected = IOException.class)
	public void testSubFileWithStartingSlash() throws IOException {
		testHandle.subFile("/bla/foo.txt");
	}

	public void testMkdir() throws IOException {
		// TODO behavior?
		testHandle.mkdir();
	}

}
