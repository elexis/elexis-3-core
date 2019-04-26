package ch.elexis.core.jcifs.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import jcifs.CloseableIterator;
import jcifs.SmbResource;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbFile;

@RunWith(Parameterized.class)
public class JCifsTest {
	
	/**
	 * NOAUTH is expected to read, but not modify
	 */
	public static final String PREFIX_NOAUTH_SAMBA = "smb://gitlab.medelexis.ch/tests/";
	//	public static final String PREFIX_NOAUTH_WIN2KSRV = "smb://fw.medelexis.ch/smb_for_unittests/";
	// bug see https://github.com/AgNO3/jcifs-ng/issues/105
	
	public static final String PREFIX_AUTH_SAMBA =
		"smb://unittest:unittest@gitlab.medelexis.ch/tests/";
	public static final String PREFIX_AUTH_WIN2KSRV =
		"smb://unittest:Unit_Test_17@fw.medelexis.ch/smb_for_unittests/";
	
	@Parameters(name = "{index}: {0}")
	public static Iterable<String> data(){
		return Arrays.asList(new String[] {
			PREFIX_NOAUTH_SAMBA, PREFIX_AUTH_SAMBA, PREFIX_AUTH_WIN2KSRV
		});
	}
	
	@Parameter
	public String prefixUrl;
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	static boolean servicesAreReachable = false;
	
	@BeforeClass
	public static void beforeClass(){
		System.setProperty("java.net.preferIPv4Stack", "true");
		try {
			servicesAreReachable = Inet4Address.getByName("gitlab.medelexis.ch").isReachable(300);
		} catch (IOException e) {
			e.printStackTrace();
			servicesAreReachable = false;
		}
	}
	
	@Test
	public void createWriteReadDeleteNonExistingFile() throws IOException{
		assumeTrue(servicesAreReachable);
		String nowString = LocalDateTime.now().toString();
		URL url = new URL(prefixUrl + "test.txt");
		if (url.getUserInfo() == null) {
			// not authenticated
			thrown.expect(SmbAuthException.class);
		}
		URLConnection openConnection = url.openConnection();
		try (SmbFile smbFile = (SmbFile) openConnection) {
			assertFalse(smbFile.exists());
			OutputStream os = openConnection.getOutputStream();
			IOUtils.write(nowString, os, Charset.defaultCharset());
			os.close();
			
			assertTrue(smbFile.exists());
			
			InputStream is = openConnection.getInputStream();
			String string = IOUtils.toString(is, "UTF-8");
			is.close();
			assertEquals(nowString, string);
			
			smbFile.delete();
			assertFalse(smbFile.exists());
		}
	}
	
	@Test
	public void createDeleteNonExistingDirectory() throws IOException{
		assumeTrue(servicesAreReachable);
		URL url = new URL(prefixUrl + "test/");
		if (url.getUserInfo() == null) {
			// not authenticated
			thrown.expect(SmbAuthException.class);
		}
		URLConnection openConnection = url.openConnection();
		try (SmbFile smbFile = (SmbFile) openConnection) {
			assertFalse(smbFile.exists());
			smbFile.mkdir();
			assertTrue(smbFile.exists());
			smbFile.delete();
			assertFalse(smbFile.exists());
		}
	}
	
	@Test
	public void listExistingDirectory() throws IOException{
		assumeTrue(servicesAreReachable);
		URL url = new URL(prefixUrl + "ZLErD3ZPHCcBj/");
		URLConnection openConnection = url.openConnection();
		try (SmbFile smbFile = (SmbFile) openConnection) {
			CloseableIterator<SmbResource> children = smbFile.children();
			assertTrue(children.hasNext());
			while (children.hasNext()) {
				SmbResource smbResource = (SmbResource) children.next();
				assertEquals("zXlpZK7UC8qwp.txt", smbResource.getName());
				assertTrue(smbResource.canRead());
			}
		}
	}
	
}
