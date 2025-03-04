package ch.elexis.core.services.vfs;

import java.io.IOException;
import java.net.InetAddress;

import org.slf4j.LoggerFactory;

public class VirtualFilesystemServiceTestUtil {

	/**
	 * NOAUTH is expected to read, but not modify
	 */
	public static String PREFIX_NOAUTH_SAMBA = "smb://gitlab.medelexis.ch/tests/";

	public static String PREFIX_AUTH_SAMBA = "smb://smbuser:qs9fifn9q1gx@gitlab.medelexis.ch/restrictedtests/";
	public static String USER = "smbuser";
	public static String PASS = "qs9fifn9q1gx";

	private static boolean serviceIsReachable;

	static {

		String server = "gitlab.medelexis.ch";

		try {
			serviceIsReachable = InetAddress.getByName(server).isReachable(200)
					|| InetAddress.getAllByName(server)[0].isReachable(200);
			if (!serviceIsReachable) {
				LoggerFactory.getLogger(VirtualFileHandle_SmbDirectory_Test.class)
						.error("Skipping Tests as server " + server + " did not respond in 300 ms");
			}
		} catch (IOException e) {
			e.printStackTrace();
			serviceIsReachable = false;
		}
	}

	public static boolean serviceIsReachable() {
		return serviceIsReachable;
	}

}
