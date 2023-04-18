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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VirtualFileHandle_WebdavDirectory_Test extends AbstractVirtualFileHandle_Webdav_Test {

	private IVirtualFilesystemHandle directoryHandle;

	@ClassRule
	public static AssumingWebdavConnection assumingWebdavConnection = new AssumingWebdavConnection(BASE_DIR);

	@BeforeClass
	public static void beforeClass() throws IOException {
		AbstractVirtualFileHandle_Webdav_Test.beforeClass();
	}

	@Before
	public void initialize() throws IOException {
		directoryHandle = service.of(BASE_DIR + "testDirectory/");
		assertNotNull(directoryHandle);
	}

	@Test
	public void a_mkdir() throws IOException {
		IVirtualFilesystemHandle dirHandle = directoryHandle.mkdir();
		assertEquals(directoryHandle.getAbsolutePath(), dirHandle.getAbsolutePath());
	}

	@Test
	public void b_isDirectory() throws IOException {
		assertTrue(directoryHandle.isDirectory());
	}

	@Test(expected = IOException.class)
	public void c_writeFails() throws IOException {
		try (OutputStream outputStream = directoryHandle.openOutputStream()) {
			IOUtils.write(randomBytes, outputStream);
		}
	}

	@Test(expected = IOException.class)
	public void d_openInputStreamFails() throws IOException {
		try (InputStream is = directoryHandle.openInputStream()) {
			byte[] byteArray = IOUtils.toByteArray(is);
			assertArrayEquals(randomBytes, byteArray);
		}
	}

	@Test
	public void e_getContentLengthFails() throws IOException {
		assertEquals(-1, directoryHandle.getContentLenght());
	}

	@Test
	public void f_getNameExtension() {
		assertEquals("testDirectory", directoryHandle.getName());
	}

	@Test
	public void g_existsCanReadCanWrite() throws IOException {
		assertTrue(directoryHandle.exists());
		assertTrue(directoryHandle.canRead());
		assertTrue(directoryHandle.canWrite());
	}

	@Test
	public void ha_list() throws IOException {
		directoryHandle.subFile("listingTestFile.txt").writeAllBytes(randomBytes);
		IVirtualFilesystemHandle[] listHandles = directoryHandle.listHandles();
		assertEquals(1, listHandles.length);
		assertEquals("listingTestFile.txt", listHandles[0].getName());
		assertTrue(listHandles[0].canWrite());
		listHandles[0].delete();
		assertFalse(listHandles[0].exists());
	}

	@Test
	public void hb_listWithFilter() throws IOException {
		directoryHandle.subFile("listingTestFile.abc").writeAllBytes(randomBytes);
		IVirtualFilesystemHandle[] listHandles = directoryHandle.listHandles(handle -> {
			String extension = handle.getExtension();
			boolean result = "abc".equalsIgnoreCase(extension);
			return result;
		});
		assertEquals(1, listHandles.length);
		assertEquals("listingTestFile.abc", listHandles[0].getName());
		assertTrue(listHandles[0].canWrite());
		listHandles[0].delete();
		assertFalse(listHandles[0].exists());
	}

	@Test
	public void i_delete() throws IOException {
		assertTrue(directoryHandle.exists());
		assertTrue(directoryHandle.isDirectory());
		directoryHandle.delete();
		assertFalse(directoryHandle.exists());
	}

	@Test
	public void j_subDir() throws IOException {
		IVirtualFilesystemHandle subDir = directoryHandle.subDir("testSubDir");
		assertEquals(directoryHandle.getAbsolutePath() + "testSubDir/", subDir.getAbsolutePath());
		assertFalse(subDir.exists());
	}

	@Test
	public void k_subFile() throws IOException {
		IVirtualFilesystemHandle subFile = directoryHandle.subFile("testSubFile");
		assertEquals(directoryHandle.getAbsolutePath() + "testSubFile", subFile.getAbsolutePath());
	}

	@Test
	public void l_getParent() throws IOException {
		IVirtualFilesystemHandle parent = directoryHandle.getParent();
		assertEquals(service.of(BASE_DIR).toURL(), parent.getURI().toURL());
	}

}
