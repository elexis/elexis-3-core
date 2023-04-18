package ch.elexis.core.services.vfs;

import java.io.IOException;
import java.util.UUID;

import org.osgi.framework.ServiceRegistration;
import org.osgi.service.url.URLStreamHandlerService;

import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.utils.OsgiServiceUtil;

public abstract class AbstractVirtualFileHandle_Webdav_Test {

	public static final String BASE_DIR = "dav://admin:admin@localhost:22808/remote.php/dav/files/admin/";

	public static IVirtualFilesystemService service;
	public static ServiceRegistration<URLStreamHandlerService> unsafeWebdavServiceRegistration;

	public static byte[] randomBytes;

	public static void beforeClass() throws IOException {
		service = OsgiServiceUtil.getService(IVirtualFilesystemService.class).get();
		randomBytes = UUID.randomUUID().toString().getBytes();
	}

	public static void afterClass() {
	}

}
