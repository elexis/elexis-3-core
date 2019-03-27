package ch.elexis.core.jpa.datasource.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Optional;

import ch.elexis.core.common.DBConnection;

/**
 * an h2 based test database connection
 */
public class TestDatabaseConnection extends DBConnection {
	
	private static final long serialVersionUID = 3263914071272054090L;
	
	/**
	 * System property for providing the location of the h2 db connection string
	 */
	public static final String TEST_DBFILE = "elexis.test.db.file";
	
	/**
	 * System property for adding ;AUTO_SERVER=TRUE to the h2 db connection string
	 */
	public static final String TEST_DBSERVER = "elexis.test.db.server";
	
	public static boolean isTestDbServer(){
		String testDbServer = System.getProperty(TEST_DBSERVER);
		if (testDbServer != null && !testDbServer.isEmpty()) {
			if (testDbServer.equalsIgnoreCase(Boolean.TRUE.toString())) {
				return true;
			}
		}
		return false;
	}
	
	public static Optional<String> getTestDbFile(){
		String testDbFile = System.getProperty(TEST_DBFILE);
		if (testDbFile != null && !testDbFile.isEmpty()) {
			return Optional.of(testDbFile);
		}
		return Optional.empty();
	}
	
	public TestDatabaseConnection(){
		this(isTestDbServer());
	}
	
	/**
	 * @param asServer
	 *            if <code>true</code> db instance can be contacted via socket
	 */
	public TestDatabaseConnection(boolean asServer){
		// initialize properties of super class ...
		rdbmsType = DBType.H2;
		databaseName = "elexisTest";
		username = "sa";
		password = "";
		Optional<String> fileLocation = getTestDbFile();
		
		String testConnectionString = System.getProperty("elexis.test.db.connectionString");
		if (testConnectionString != null) {
			connectionString = testConnectionString;
		} else {
			if (asServer) {
				if(fileLocation.isPresent()) {
					connectionString = "jdbc:h2:" + fileLocation.get() + ";AUTO_SERVER=TRUE";					
				} else {
					connectionString = "jdbc:h2:~/elexisTest/elexisTest;AUTO_SERVER=TRUE";
				}
			} else {
				connectionString = "jdbc:h2:mem:elexisTest;DB_CLOSE_DELAY=-1";
			}
		}
		
		String trace = System.getProperty("elexis.test.dbtrace");
		if (trace != null && "true".equalsIgnoreCase(trace)) {
			connectionString += ";TRACE_LEVEL_SYSTEM_OUT=2";
		}

		if (asServer) {
			// only delete the automatically created db, not if user specified a file location
			if (!fileLocation.isPresent()) {
				Runtime.getRuntime().addShutdownHook(new Thread() {
					public void run(){
						try {
							Path dbdir = Paths.get(System.getenv("HOME"), "/elexisTest");
							System.out.println("Cleaning up test database in [" + dbdir + "]");
							Files.walk(dbdir).sorted(Comparator.reverseOrder()).map(Path::toFile)
								.forEach(File::delete);
						} catch (IOException e) {
							System.out.println("Error deleting database.");
							e.printStackTrace();
						}
					}
				});
			}
		}
	}
}
