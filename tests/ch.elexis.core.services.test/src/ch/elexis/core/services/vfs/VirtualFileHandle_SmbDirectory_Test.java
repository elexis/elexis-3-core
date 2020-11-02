package ch.elexis.core.services.vfs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.net.InetAddress;

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
	public void testListHandles() throws IOException{
		assumeTrue(serviceIsReachable);
		
		IVirtualFilesystemHandle[] listHandles = service.of(PREFIX_NOAUTH_SAMBA).listHandles();
		// 1 directory, 2 files
		assertEquals(3, listHandles.length);
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
