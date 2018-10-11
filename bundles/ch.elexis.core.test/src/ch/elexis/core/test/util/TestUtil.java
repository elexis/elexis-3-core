package ch.elexis.core.test.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TestUtil {

	public static String loadFile(Class<?> classLoader, String resourceName) throws IOException{
		StringBuffer sb = new StringBuffer();
		String line;
		InputStream inputStream = classLoader.getResourceAsStream(resourceName);
		if (inputStream == null) {
			throw new IOException("Could not load resource [" + resourceName + "]");
		}
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		}
		return sb.toString();
	}
	
}

