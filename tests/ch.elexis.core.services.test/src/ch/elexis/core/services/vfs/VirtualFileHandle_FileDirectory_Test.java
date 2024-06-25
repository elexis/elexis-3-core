package ch.elexis.core.services.vfs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.utils.OsgiServiceUtil;

public class VirtualFileHandle_FileDirectory_Test extends AbstractVirtualFileHandleTest {

	// TODO change to directory

	@BeforeClass
	public static void beforeClass() throws IOException {
		service = OsgiServiceUtil.getService(IVirtualFilesystemService.class).get();
		tempDirectory = Files.createTempDirectory("virtualFilesystemTest_filedirectory");
		testDirectory = new File(tempDirectory.toFile(), "subDir/");
		assertTrue(testDirectory.mkdir());
		testDirectory.deleteOnExit();
		testHandle = service.of(testDirectory);
		assertTrue(testHandle.isDirectory());
	}

	@AfterClass
	public static void afterClass() throws IOException {
		FileUtils.deleteDirectory(tempDirectory.toFile());
	}

	@Test(expected = IOException.class)
	public void testOpenOutputStream() throws IOException {
		testHandle.openOutputStream();
	}

	@Test
	public void testCopyTo() throws IOException {
		File copyToFile = new File(testDirectory, "copyToFile");
		copyToFile.createNewFile();
		IVirtualFilesystemHandle srcFile = service.of(copyToFile);
		IVirtualFilesystemHandle dstFile = srcFile.copyTo(testHandle);
		File _srcFile = srcFile.toFile().get();
		File _dstFile = dstFile.toFile().get();
		assertTrue(_srcFile.canRead());
		assertTrue(_dstFile.canRead());
		assertEquals(_srcFile.length(), _dstFile.length());
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
		File fileWithSpace = new File(testHandle.toFile().get(), "space listingFile.txt");
		assertTrue(fileWithSpace.createNewFile());
		IVirtualFilesystemHandle[] listHandles = testHandle.listHandles();
		assertEquals(2, listHandles.length);
		for (IVirtualFilesystemHandle ivfsh : listHandles) {
			if (ivfsh.getName().contains("space")) {
				assertEquals(fileWithSpace, ivfsh.toFile().get());
			} else {
				assertEquals(file, ivfsh.toFile().get());
			}
		}
		assertTrue(file.delete());
		assertTrue(fileWithSpace.delete());
	}

	@Test
	public void testListHandlesIVirtualFilesystemhandleFilter() throws IOException {
		File file = new File(testHandle.toFile().get(), "listingFile.txt");
		assertTrue(file.createNewFile());
		File file1 = new File(testHandle.toFile().get(), "test file.txt");
		assertTrue(file1.createNewFile());
		File file2 = new File(testHandle.toFile().get(), "listingFile.txta");
		assertTrue(file2.createNewFile());
		IVirtualFilesystemHandle[] listHandles = testHandle
				.listHandles(handle -> "txt".equalsIgnoreCase(handle.getExtension()));
		assertEquals(2, listHandles.length);
		assertTrue(file.delete());
		assertTrue(file1.delete());
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
		assertEquals("subDir", testHandle.getName());
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
	public void testMoveTo() throws IOException {
		File moveToFile = new File(tempDirectory.toFile(), "moveToFile_2");
		assertTrue(moveToFile.createNewFile());

		IVirtualFilesystemHandle vfh_moveTo = service.of(moveToFile);
		IVirtualFilesystemHandle vfh_target = vfh_moveTo.moveTo(service.of(testDirectory));

		assertNotEquals(vfh_moveTo, vfh_target);
		assertFalse(moveToFile.exists());
		assertTrue(vfh_target.toFile().get().exists());
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

	@Test(expected = IllegalArgumentException.class)
	public void testSubFileWithStartingSlash() throws IOException {
		testHandle.subFile("/bla/foo.txt");
	}

	@Test
	public void testMkdirs() throws IOException {
		IVirtualFilesystemHandle subMkdirs = service.of(testDirectory).subDir("parent1").subDir("parent2")
				.subDir("parent3").mkdirs();
		assertTrue(subMkdirs.exists());
		service.of(testDirectory).subDir("parent1").delete();
	}

	@Test(expected = IOException.class)
	public void openInputStreamFails() throws IOException {
		try (InputStream is = testHandle.openInputStream()) {
			String string = IOUtils.toString(is, Charset.defaultCharset());
			System.out.println(string);
		}
	}
}
