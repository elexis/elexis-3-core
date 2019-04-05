package ch.elexis.core.jcifs.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.time.LocalDateTime;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import jcifs.CloseableIterator;
import jcifs.SmbResource;
import jcifs.smb.SmbFile;

public class JCifsTest {
	
	@Test
	public void createWriteNonExistingFile() throws IOException{
		URL url = new URL("smb://unittest:unittest@gitlab.medelexis.ch/tests/test.txt");
		URLConnection openConnection = url.openConnection();
		System.out.println(openConnection.getClass());
		try (SmbFile smbFile = (SmbFile) openConnection) {
			OutputStream os = openConnection.getOutputStream();
			IOUtils.write(LocalDateTime.now().toString(), os, Charset.defaultCharset());
			smbFile.delete();
		}
		
	}
	
	@Test
	public void basicSmbDirectory() throws IOException{
		URL url = new URL("smb://unittest:unittest@gitlab.medelexis.ch/tests/");
		URLConnection openConnection = url.openConnection();
		try (SmbFile smbFile = (SmbFile) openConnection) {
			CloseableIterator<SmbResource> children = smbFile.children();
			assertTrue(children.hasNext());
			while (children.hasNext()) {
				SmbResource smbResource = (SmbResource) children.next();
				assertTrue(smbResource.canRead());
			}
		}
	}
	
	@Test
	public void createDeleteNonExistingDirectory() throws IOException{
		URL url = new URL("smb://unittest:unittest@gitlab.medelexis.ch/tests/test/");
		URLConnection openConnection = url.openConnection();
		try (SmbFile smbFile = (SmbFile) openConnection) {
			smbFile.mkdir();
			smbFile.delete();
		}
	}
	
}
