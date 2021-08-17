package ch.elexis.core.services.vfs;

import static ch.elexis.core.services.vfs.VirtualFilesystemServiceTestUtil.PREFIX_AUTH_SAMBA;
import static ch.elexis.core.services.vfs.VirtualFilesystemServiceTestUtil.serviceIsReachable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.utils.OsgiServiceUtil;

public class VirtualFileHandle_Combined_Test extends AbstractVirtualFileHandleTest {
	
	@BeforeClass
	public static void beforeClass() throws IOException{
		service = OsgiServiceUtil.getService(IVirtualFilesystemService.class).get();
		tempDirectory = Files.createTempDirectory("virtualFilesystemTest_combined");
		testDirectory = new File(tempDirectory.toFile(), "subDir/");
		assertTrue(testDirectory.mkdir());
		testDirectory.deleteOnExit();
		testHandle = service.of(testDirectory);
		assertTrue(testHandle.isDirectory());
	}
	
	@AfterClass
	public static void afterClass() throws IOException{
		FileUtils.deleteDirectory(tempDirectory.toFile());
	}
	
	private List<String> FILENAMES_TO_TEST = Arrays.asList(
		"Microsoft Word - [130121]-Varizenabklärung Frau Muster NM.doc.pdf",
		"Microsoft Word - [#130121]-Varizenabklärung Frau Muster NM.doc.pdf",
		"Münlar Knut 9.2.1746 Bescheinigung.pdf", "PF_27245_17491.pdf Schnaitar ranh.pdf",
		"[Schopl Rbdssea 3 #178151]-Coloskopie_19100224.pdf",
		"[Schopl Rbdssea 3 178151]-Coloskopie_19100224.pdf", "2021-03-15-11-43-59-1.pdf",
		"[#75503]-Bericht Dr.BELLI 1.3.21-1.pdf",
		"Bericht_'Operationsbericht_Viszeralchirurgie_vom_26.02.2021'_für_Calogero_Miraglia_Fagiano.pdf");
	
	@Test
	public void copyFromFileToSmb() throws IOException{
		assumeTrue(serviceIsReachable());
		System.out.println(tempDirectory.toFile());
		
		IVirtualFilesystemHandle remoteSubDir =
			service.of(PREFIX_AUTH_SAMBA).subDir("combined-test");
		IVirtualFilesystemHandle _remoteSubDir = remoteSubDir.mkdir();
		
		for (String filename : FILENAMES_TO_TEST) {
			
			File testFile = new File(testDirectory, filename);
			Files.write(testFile.toPath(), "meaninglessTestTextThatProvidesSomeContent".getBytes(),
				StandardOpenOption.CREATE);
			
			IVirtualFilesystemHandle source = service.of(testFile);
			IVirtualFilesystemHandle target = _remoteSubDir.subFile(filename);
			
			IVirtualFilesystemHandle _target = source.copyTo(target);
			assertTrue(_target.exists());
			assertEquals(filename, _target.getName());
			
			String _loadedString = PREFIX_AUTH_SAMBA + "combined-test/" + filename;
			IVirtualFilesystemHandle _loaded = service.of(_loadedString);
			assertTrue(_loaded.toURL().toString(), _loaded.exists());
			assertEquals(filename, _loaded.getName());
			
			assertTrue(_target.canRead());
			_target.delete();
			assertFalse(_target.exists());
		}
	}
	
}
