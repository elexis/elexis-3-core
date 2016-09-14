package ch.elexis.core.findings.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ServerDatabaseInitailizer {

	@SuppressWarnings("rawtypes")
	public void initalize() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
			NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
		Class testDatabaseInitializerClass = getClass().getClassLoader()
					.loadClass("info.elexis.server.core.connector.elexis.jpa.test.TestDatabaseInitializer");
		Method initializeDbMethod = testDatabaseInitializerClass.getMethod("initializeDb", null);
		Object testDatabaseInitializer = testDatabaseInitializerClass.newInstance();
		initializeDbMethod.invoke(testDatabaseInitializer, null);
	}
}
