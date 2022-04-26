package ch.elexis.core.services.vfs;

import java.io.IOException;
import java.net.InetAddress;

import org.junit.BeforeClass;
import org.slf4j.LoggerFactory;

import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.utils.OsgiServiceUtil;

public class VirtualFileHandle_SmbFile_Test {

	private static IVirtualFilesystemService service;
	private static boolean serviceIsReachable;

	@BeforeClass
	public static void beforeClass() throws IOException {
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

}
