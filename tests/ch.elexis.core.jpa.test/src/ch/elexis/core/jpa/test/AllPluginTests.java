package ch.elexis.core.jpa.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.elexis.core.jpa.entitymanger.ExecuteScript;
import ch.elexis.core.jpa.entitymanger.InitPersistenceUnit;

@RunWith(Suite.class)
@SuiteClasses({
	InitPersistenceUnit.class, ExecuteScript.class
})
public class AllPluginTests {
	
	public static String loadFile(String string) throws IOException{
		BufferedReader reader = null;
		StringBuffer sb = new StringBuffer();
		String line;
		try {
			reader = new BufferedReader(
				new InputStreamReader(AllPluginTests.class.getResourceAsStream(string)));
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		return sb.toString();
	}
}
