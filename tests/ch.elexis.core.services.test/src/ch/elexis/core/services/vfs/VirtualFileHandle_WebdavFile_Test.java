package ch.elexis.core.services.vfs;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VirtualFileHandle_WebdavFile_Test extends AbstractVirtualFileHandle_Webdav_Test {

	IVirtualFilesystemHandle handle;

	@ClassRule
	public static AssumingWebdavConnection assumingWebdavConnection = new AssumingWebdavConnection(BASE_DIR);

	@BeforeClass
	public static void beforeClass() throws IOException {
		AbstractVirtualFileHandle_Webdav_Test.beforeClass();
	}

	@AfterClass
	public static void afterClass() {
		AbstractVirtualFileHandle_Webdav_Test.afterClass();
	}

	@Before
	public void initialize() throws IOException {
		handle = service.of(BASE_DIR + "testfile.txt");
		assertNotNull(handle);
	}

	@Test
	public void a_createFile() throws IOException, InterruptedException {
		try (OutputStream outputStream = handle.openOutputStream()) {
			IOUtils.write(randomBytes, outputStream);
		}
		Thread.sleep(200); // asynchronously written, wait for it, else get fails
	}

	@Test
	public void b_isDirectory() throws IOException {
		assertFalse(handle.isDirectory());
	}

	@Test
	public void c_openInputStream() throws IOException {
		try (InputStream is = handle.openInputStream()) {
			byte[] byteArray = IOUtils.toByteArray(is);
			assertArrayEquals(randomBytes, byteArray);
		}
	}

	@Test
	public void ca_getContentLength() throws IOException {
		assertEquals(36, handle.getContentLenght());
	}

	@Test
	public void d_getNameExtension() {
		assertEquals("testfile.txt", handle.getName());
		assertEquals("txt", handle.getExtension());
	}

	@Test
	public void e_existsCanReadCanWrite() throws IOException {
		assertTrue(handle.exists());
		assertTrue(handle.canRead());
		assertTrue(handle.canWrite());
	}

	@Test
	public void ee_moveToAnotherDirectory() throws IOException {
		IVirtualFilesystemHandle ftm = service.of(BASE_DIR + "testFileToMove.txt");
		ftm.writeAllBytes(randomBytes);
		IVirtualFilesystemHandle dtm = service.of(BASE_DIR + "moveTo/");
		dtm.mkdir();
		IVirtualFilesystemHandle targetFtm = ftm.moveTo(dtm);
		assertFalse(ftm.exists());
		assertEquals(BASE_DIR + "moveTo/testFileToMove.txt", targetFtm.getURI().toString());
		assertTrue(targetFtm.exists());
		dtm.delete();
		assertFalse(dtm.exists());
		assertFalse(targetFtm.exists());
	}

	@Test
	public void ee_copyToAnotherDirectory() throws IOException {
		IVirtualFilesystemHandle ftm = service.of(BASE_DIR + "testFileToMove.txt");
		ftm.writeAllBytes(randomBytes);
		IVirtualFilesystemHandle dtm = service.of(BASE_DIR + "copyTo/");
		dtm.mkdir();
		IVirtualFilesystemHandle targetFtm = ftm.copyTo(dtm);
		assertTrue(ftm.exists());
		assertEquals(BASE_DIR + "copyTo/testFileToMove.txt", targetFtm.getURI().toString());
		assertTrue(targetFtm.exists());
		dtm.delete();
		assertFalse(dtm.exists());
		assertFalse(targetFtm.exists());
	}

	@Test
	public void f_delete() throws IOException {
		handle.delete();
	}

	@Test
	public void g_subDir() throws IOException {
		IVirtualFilesystemHandle subDir = handle.subDir("testSubDir");
		assertEquals(handle.getAbsolutePath() + "/testSubDir/", subDir.getAbsolutePath());
		assertFalse(subDir.exists());
	}

	@Test(expected = IOException.class)
	public void h_subFileFails() throws IOException {
		handle.subFile("testSubFile");
	}

	@Test
	public void i_getParent() throws IOException {
		IVirtualFilesystemHandle parent = handle.getParent();
		assertEquals(service.of(BASE_DIR).toURL(), parent.getURI().toURL());
	}

	@Test(expected = IOException.class)
	public void j_listFails() throws IOException {
		handle.listHandles();
	}

	@Test(expected = IOException.class)
	public void k_listWithFilterFails() throws IOException {
		handle.listHandles(handle -> {
			String extension = handle.getExtension();
			boolean result = "txt".equalsIgnoreCase(extension);
			return result;
		});
	}

	@Test
	public void z_mkdir() throws IOException {
		assertFalse(handle.exists());
		assertFalse(handle.isDirectory());
		handle.mkdir();
		assertTrue(handle.exists());
		assertTrue(handle.isDirectory());
	}

	@Test
	public void zz_mkdirDelete() throws IOException {
		assertTrue(handle.isDirectory());
		handle.delete();
		assertFalse(handle.exists());
	}
}
