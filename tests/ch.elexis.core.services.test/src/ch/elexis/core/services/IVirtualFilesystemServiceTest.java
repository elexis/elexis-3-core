package ch.elexis.core.services;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.utils.OsgiServiceUtil;

@RunWith(Parameterized.class)
public class IVirtualFilesystemServiceTest {
	
	private static IVirtualFilesystemService vfss;
	private static Path tempDirectory;
	
	private static File tempFile;
	
	@Parameters(name = "{index}: [{0}]")
	public static Iterable<String> data() throws IOException{
		vfss = OsgiServiceUtil.getService(IVirtualFilesystemService.class).get();
		tempDirectory = Files.createTempDirectory("virtualFilesystemTest");
		tempFile = Files.createTempFile(tempDirectory, "test", "txt").toFile();
		String localTempFileUri = tempFile.toURI().toURL().toString();
		String localTempFile = tempFile.toString();
		
		return Arrays.asList(new String[] {
			localTempFileUri, // file:/bla/...
			localTempFile, // /bla/...
			tempDirectory.toUri().toURL().toString(), // directory
			"https://raw.githubusercontent.com/elexis/elexis-3-core/master/.gitlab-ci.yml",
			"smb://unittest:unittest@gitlab.medelexis.ch/tests/test.txt",
			"\\\\gitlab.medelexis.ch\\tests\\test.txt" // unc convert to smb
		});
	}
	
	@Parameter
	public String source;
	
	@Test
	public void openStream() throws IOException{
		try (InputStream is = vfss.of(source).openInputStream()) {
			String string = IOUtils.toString(is, Charset.defaultCharset());
			System.out.println(string);
		}
	}
	
	@Test
	public void copy() throws IOException{
		// TODO what do directories do?!
		File file = Files.createTempFile(tempDirectory, "test", "txt").toFile();
		file.deleteOnExit();
		vfss.of(source).copyTo(vfss.of(file.toURI().toURL().toString()));
	}
	
	@Test
	public void getParent() throws IOException {
		System.out.println(vfss.of(source).getParent().getAbsolutePath());
	}
	
	@Test
	public void mkdir_deleteDir() throws IOException {
		IVirtualFilesystemHandle mkdir = vfss.of(source).subdir("test").mkdir();
		assertTrue(mkdir.exists());
		mkdir.delete();
		assertFalse(mkdir.exists());
	}
	
}
