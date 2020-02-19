package ch.elexis.core.services.vfs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;

public abstract class AbstractVirtualFileHandleTest {

	static IVirtualFilesystemHandle testDirectoryHandle;	
	static IVirtualFilesystemService service;	
	
	@Test
	public void testOpenInputStream() throws IOException{
		try (InputStream is = testDirectoryHandle.openInputStream()) {
			String string = IOUtils.toString(is, Charset.defaultCharset());
			System.out.println(string);
		}
	}



	
}
