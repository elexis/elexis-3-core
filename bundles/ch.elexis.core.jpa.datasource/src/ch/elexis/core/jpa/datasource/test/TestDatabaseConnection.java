package ch.elexis.core.jpa.datasource.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import ch.elexis.core.common.DBConnection;

/**
 * an h2 based test database connection
 */
public class TestDatabaseConnection extends DBConnection {
	
	private static final long serialVersionUID = 3263914071272054090L;
	
	public TestDatabaseConnection(){
		this(false);
	}
	
	/**
	 * @param asServer
	 *            if <code>true</code> db instance can be contacted via socket
	 */
	public TestDatabaseConnection(boolean asServer){
		if (asServer) {
			connectionString = "jdbc:h2:~/elexisTest/elexisTest;AUTO_SERVER=TRUE";
		} else {
			connectionString = "jdbc:h2:mem:elexisTest;DB_CLOSE_DELAY=-1";
		}
		String trace = System.getProperty("elexis.test.dbtrace");
		if (trace != null && "true".equalsIgnoreCase(trace)) {
			connectionString += ";TRACE_LEVEL_SYSTEM_OUT=2";
		}
		rdbmsType = DBType.H2;
		username = "sa";
		password = "";
		
		if (asServer) {
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
