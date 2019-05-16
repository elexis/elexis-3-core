package ch.elexis.core.services.vfs;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.utils.OsgiServiceUtil;

public class VirtualFilesystemServiceTest {
	
	IVirtualFilesystemService service =
		OsgiServiceUtil.getService(IVirtualFilesystemService.class).get();
	
	@Test
	public void of_unixoid() throws IOException{
		assertEquals(new File("/tmp/test.txt"), service.of("/tmp/test.txt").toFile().get());
		service.of("//tmp/test.txt");
		service.of("file://tmp/test.txt");
		service.of("file:///tmp/test.txt");
		
	}
	
//	@Test
//	public void of_windows() throws IOException{
//		assertEquals(new File("/tmp/test.txt"), service.of("C:/tmp/test.txt").toFile().get());
//		service.of("\\server\test.txt");
//	}
	
}
