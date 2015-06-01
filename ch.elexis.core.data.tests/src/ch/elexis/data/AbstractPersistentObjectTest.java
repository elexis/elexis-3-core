package ch.elexis.data;

import static org.junit.Assert.assertNotNull;
import ch.elexis.ResourceManager;
import ch.rgw.tools.JdbcLink;

public abstract class AbstractPersistentObjectTest {
	
	/**
	 * create a H2-JdbcLink with an initialized db for elexis.
	 * 
	 * the creation script is taken from the rsc directory of the host plugin when running a
	 * Plugin-Test
	 */
	protected static JdbcLink initDB(){
		return initDB("h2");
	}
	
	/**
	 * create a JdbcLink with an initialized db for elexis the creation script is taken from the rsc
	 * directory of the host plugin when running a Plugin-Test
	 */
	protected static JdbcLink initDB(String dbflavor){
		JdbcLink link = null;
		ResourceManager rsc = ResourceManager.getInstance();
		String pluginPath = rsc.getResourceLocationByName("/createDB.script");
		int end = pluginPath.lastIndexOf('/');
		end = pluginPath.lastIndexOf('/', end - 1);
		pluginPath = pluginPath.substring(0, end);
		if (dbflavor == "h2")
			link = new JdbcLink("org.h2.Driver", "jdbc:h2:mem:test_mem", "hsql");
		else if (dbflavor == "mysql")
			link = JdbcLink.createMySqlLink("localhost", "unittests");
		else if (dbflavor == "postgresql")
			link = JdbcLink.createPostgreSQLLink("localhost", "unittests");
		
		assertNotNull(link);
		link.connect("elexis", "elexisTest");
		PersistentObject.connect(link);
		return link;
	}
	
}
