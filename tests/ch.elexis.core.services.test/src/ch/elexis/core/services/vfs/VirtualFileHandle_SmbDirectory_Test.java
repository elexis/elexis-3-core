package ch.elexis.core.services.vfs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemhandleFilter;
import ch.elexis.core.utils.OsgiServiceUtil;

public class VirtualFileHandle_SmbDirectory_Test {
	
	private static IVirtualFilesystemService service;
	private static boolean serviceIsReachable;
	
	/**
	 * NOAUTH is expected to read, but not modify
	 */
	public static String PREFIX_NOAUTH_SAMBA = "smb://gitlab.medelexis.ch/tests/";
	
	public static String PREFIX_AUTH_SAMBA =
		"smb://smbuser:qs9fifn9q1gx@gitlab.medelexis.ch/restrictedtests/";
	public static String USER = "smbuser";
	public static String PASS = "qs9fifn9q1gx";
	
	@BeforeClass
	public static void beforeClass() throws IOException{
		service = OsgiServiceUtil.getService(IVirtualFilesystemService.class).get();
		String server = "gitlab.medelexis.ch";
		
		try {
			serviceIsReachable = InetAddress.getByName(server).isReachable(300)
				|| InetAddress.getAllByName(server)[0].isReachable(300);
			if (!serviceIsReachable) {
				LoggerFactory.getLogger(VirtualFileHandle_SmbDirectory_Test.class)
					.error("Skipping Tests as server " + server + " did not respond in 300 ms");
			}
		} catch (IOException e) {
			e.printStackTrace();
			serviceIsReachable = false;
		}
	}
	
	@Test
	public void canRead() throws IOException{
		assumeTrue(serviceIsReachable);
		assertTrue(service.of(PREFIX_NOAUTH_SAMBA + "testfile.txt").canRead());
		assertTrue(service.of("\\\\gitlab.medelexis.ch\\tests\\testfile.txt").canRead());
		//		assertTrue(service.of(PREFIX_NOAUTH_SAMBA + "test file.txt").canRead());
		//		assertTrue(service.of("\\\\gitlab.medelexis.ch\\tests\\test file.txt").canRead());
		
	}
	
	@Test
	public void isDirectory() throws IOException{
		assumeTrue(serviceIsReachable);
		assertTrue(service.of(PREFIX_NOAUTH_SAMBA + "/ZLErD3ZPHCcBj").isDirectory());
		assertFalse(
			service.of(PREFIX_NOAUTH_SAMBA + "/ZLErD3ZPHCcBj/zXlpZK7UC8qwp.txt").isDirectory());
	}
	
	@Test
	public void testListHandles() throws IOException{
		assumeTrue(serviceIsReachable);
		
		IVirtualFilesystemHandle[] listHandles = service.of(PREFIX_NOAUTH_SAMBA).listHandles();
		assertEquals(5, listHandles.length);
		
		try (InputStream is = listHandles[1].openInputStream()) {
			// #21875 to test if spaces are correctly opened
		}
	}
	
	@Test
	public void testCreateAndMoveToAndDelete() throws IOException{
		assumeTrue(serviceIsReachable);

		IVirtualFilesystemHandle dir = service.of(PREFIX_AUTH_SAMBA);
		IVirtualFilesystemHandle subFile = dir.subFile("Test File.txt");
		try (PrintWriter p = new PrintWriter(subFile.openOutputStream())) {
			p.write("TestFile\n");
		}
		assertTrue(subFile.exists());
		assertTrue(subFile.canRead());
		IVirtualFilesystemHandle subFileRenamed = dir.subFile("Test File renamed.txt");
		
		subFile.moveTo(subFileRenamed);
		
		assertFalse(subFile.exists());
		assertTrue(subFileRenamed.exists());
		assertTrue(subFileRenamed.canRead());
		subFileRenamed.delete();
		assertFalse(subFileRenamed.exists());
	}
	
	@Test
	public void testCreateAndMoveToBetweenHosts() throws UnknownHostException, IOException {
		assumeTrue(serviceIsReachable);
		
		boolean ee_medevit_atIsReachable = InetAddress.getByName("192.168.0.23").isReachable(300)
				|| InetAddress.getAllByName("192.168.0.23")[0].isReachable(300);
		
		assumeTrue(ee_medevit_atIsReachable);
		
		IVirtualFilesystemHandle dir = service.of("smb://192.168.0.23/scan/processed/");
		IVirtualFilesystemHandle subFile = dir.subFile("Test File.txt");
		try (PrintWriter p = new PrintWriter(subFile.openOutputStream())) {
			p.write("TestFile\n");
		}
		assertTrue(subFile.exists());
		assertTrue(subFile.canRead());
		
		IVirtualFilesystemHandle dirOtherResource = service.of(PREFIX_AUTH_SAMBA);
		IVirtualFilesystemHandle subFileRenamed = dirOtherResource.subFile("Test File renamed.txt");
		
		subFile.moveTo(subFileRenamed);
		
		assertFalse(subFile.exists());
		assertTrue(subFileRenamed.exists());
		assertTrue(subFileRenamed.canRead());
		subFileRenamed.delete();
		assertFalse(subFileRenamed.exists());
	}
	
	@Test
	public void testCopyToAndDelete() throws IOException{
		assumeTrue(serviceIsReachable);
		
		IVirtualFilesystemHandle[] listHandles = service.of(PREFIX_NOAUTH_SAMBA)
			.listHandles(handle -> "pdf".equalsIgnoreCase(handle.getExtension()));
		assertEquals(2, listHandles.length);
		
		IVirtualFilesystemHandle target =
			service.of(PREFIX_AUTH_SAMBA).subFile(listHandles[0].getName());
		IVirtualFilesystemHandle _target = listHandles[0].copyTo(target);
		assertTrue(_target.exists());
		assertTrue(_target.canRead());
		_target.delete();
		assertFalse(_target.exists());
	}
	
	@Test
	public void testListHandlesIVirtualFilesystemhandleFilter() throws IOException{
		assumeTrue(serviceIsReachable);
		
		IVirtualFilesystemhandleFilter ivfh = new IVirtualFilesystemhandleFilter() {
			@Override
			public boolean accept(IVirtualFilesystemHandle handle){
				String extension = handle.getExtension();
				boolean result = "txt".equalsIgnoreCase(extension);
				return result;
			}
		};
		
		IVirtualFilesystemHandle[] listHandles = service.of(PREFIX_NOAUTH_SAMBA).listHandles(ivfh);
		assertEquals(2, listHandles.length);
	}
	
}
