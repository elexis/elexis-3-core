package ch.elexis.core.serial.test;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.elexis.core.serial.ConnectionChunkParser;

@RunWith(Suite.class)
@SuiteClasses({
	ConnectionChunkParser.class
})
public class AllTests {
	
	public static byte[] readBytes(String path) throws IOException{
		try (InputStream in = AllTests.class.getResourceAsStream(path)) {
			return IOUtils.toByteArray(in);
		}
	}
}
