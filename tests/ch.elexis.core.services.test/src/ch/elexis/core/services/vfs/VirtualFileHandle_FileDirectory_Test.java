package ch.elexis.core.services.vfs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.utils.OsgiServiceUtil;

public class VirtualFileHandle_FileDirectory_Test extends AbstractVirtualFileHandleTest {
	
	static Path tempDirectory;
	static File testDirectory;
	
	// TODO change to directory
	
	@BeforeClass
	public static void beforeClass() throws IOException{
		service = OsgiServiceUtil.getService(IVirtualFilesystemService.class).get();
		tempDirectory = Files.createTempDirectory("virtualFilesystemTest_filedirectory");
		testDirectory = new File(tempDirectory.toFile(), "subDir/");
		assertTrue(testDirectory.mkdir());
		testDirectory.deleteOnExit();
		testDirectoryHandle = service.of(testDirectory);
		assertTrue(testDirectoryHandle.isDirectory());
	}
	
	@AfterClass
	public static void afterClass() throws IOException{
		FileUtils.deleteDirectory(tempDirectory.toFile());
	}
	
	@Test(expected = IOException.class)
	public void testOpenOutputStream() throws IOException{
		testDirectoryHandle.openOutputStream();
	}
	
	@Test
	public void testCopyTo() throws IOException{
		File copyToFile = new File(testDirectory, "copyToFile");
		copyToFile.createNewFile();
		IVirtualFilesystemHandle srcFile = service.of(copyToFile);
		IVirtualFilesystemHandle dstFile = srcFile.copyTo(testDirectoryHandle);
		File _srcFile = srcFile.toFile().get();
		File _dstFile = dstFile.toFile().get();
		assertTrue(_srcFile.canRead());
		assertTrue(_dstFile.canRead());
		assertEquals(_srcFile.length(), _dstFile.length());
	}
	
	@Test
	public void testGetParent() throws IOException{
		IVirtualFilesystemHandle parent = testDirectoryHandle.getParent();
		assertEquals(tempDirectory.toFile(), parent.toFile().get());
	}
	
	@Test
	public void testListHandles() throws IOException{
		File file = new File(testDirectoryHandle.toFile().get(), "listingFile.txt");
		assertTrue(file.createNewFile());
		File fileWithSpace = new File(testDirectoryHandle.toFile().get(), "space listingFile.txt");
		assertTrue(fileWithSpace.createNewFile());
		IVirtualFilesystemHandle[] listHandles = testDirectoryHandle.listHandles();
		assertEquals(2, listHandles.length);
		for (IVirtualFilesystemHandle ivfsh : listHandles) {
			if(ivfsh.getName().contains("space")) {
				assertEquals(fileWithSpace, ivfsh.toFile().get());
			} else {
				assertEquals(file, ivfsh.toFile().get());
			}
		}
		assertTrue(file.delete());
		assertTrue(fileWithSpace.delete());
	}
	
	@Test
	public void testListHandlesIVirtualFilesystemhandleFilter() throws IOException{
		File file = new File(testDirectoryHandle.toFile().get(), "listingFile.txt");
		assertTrue(file.createNewFile());
		File file1 = new File(testDirectoryHandle.toFile().get(), "test file.txt");
		assertTrue(file1.createNewFile());
		File file2 = new File(testDirectoryHandle.toFile().get(), "listingFile.txta");
		assertTrue(file2.createNewFile());
		IVirtualFilesystemHandle[] listHandles = testDirectoryHandle
			.listHandles(handle -> "txt".equalsIgnoreCase(handle.getExtension()));
		assertEquals(2, listHandles.length);
		assertTrue(file.delete());
		assertTrue(file1.delete());
		assertTrue(file2.delete());
	}
	
	@Test
	public void testDelete() throws IOException{
		testDirectoryHandle.delete();
		assertFalse(testDirectory.exists());
		assertTrue(testDirectory.mkdir());
	}
	
	@Test
	public void testToURL() throws MalformedURLException{
		assertEquals(testDirectory.toURI().toURL(), testDirectoryHandle.toURL());
	}
	
	@Test
	public void testIsDirectory() throws IOException{
		assertTrue(testDirectoryHandle.isDirectory());
	}
	
	@Test
	public void testToFile(){
		assertEquals(testDirectory, testDirectoryHandle.toFile().get());
	}
	
	@Test
	public void testGetExtension(){
		assertEquals("", testDirectoryHandle.getExtension());
	}
	
	@Test
	public void testExists() throws IOException{
		assertTrue(testDirectory.delete());
		assertFalse(testDirectoryHandle.exists());
		assertTrue(testDirectory.mkdir());
		assertTrue(testDirectoryHandle.exists());
	}
	
	@Test
	public void testGetName(){
		assertEquals("subDir", testDirectoryHandle.getName());
	}
	
	@Test
	public void testCanRead(){
		assertTrue(testDirectoryHandle.canRead());
	}
	
	@Test
	public void testGetAbsolutePath(){
		assertEquals(testDirectory.toURI().toString(), testDirectoryHandle.getAbsolutePath());
	}
	
	@Test
	public void testMoveTo() throws IOException{
		File moveToFile = new File(tempDirectory.toFile(), "moveToFile_2");
		assertTrue(moveToFile.createNewFile());
		
		IVirtualFilesystemHandle vfh_moveTo = service.of(moveToFile);
		IVirtualFilesystemHandle vfh_target = vfh_moveTo.moveTo(service.of(testDirectory));
		
		assertNotEquals(vfh_moveTo, vfh_target);
		assertFalse(moveToFile.exists());
		assertTrue(vfh_target.toFile().get().exists());
	}
	
	@Test
	public void testSubDir() throws IOException{
		IVirtualFilesystemHandle subDir = testDirectoryHandle.subDir("subdir");
		assertFalse(subDir.exists());
		subDir.mkdir();
		assertTrue(subDir.exists());
	}
	
	@Test
	public void testSubFile() throws IOException{
		IVirtualFilesystemHandle subFile = testDirectoryHandle.subFile("subfile");
		assertFalse(subFile.exists());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSubFileWithStartingSlash() throws IOException{
		testDirectoryHandle.subFile("/bla/foo.txt");
	}
	
	public void testMkdir() throws IOException{
		// TODO behavior?
		testDirectoryHandle.mkdir();
	}
	
	@Test(expected = FileNotFoundException.class)
	public void testOpenInputStream() throws IOException{
		try (InputStream is = testDirectoryHandle.openInputStream()) {
			String string = IOUtils.toString(is, Charset.defaultCharset());
			System.out.println(string);
		}
	}
}
