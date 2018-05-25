package ch.elexis.core.jpa.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.elexis.core.common.DBConnection;
import ch.elexis.core.common.DBConnection.DBType;
import ch.elexis.core.jpa.entitymanger.InitPersistenceUnit;

@RunWith(Suite.class)
@SuiteClasses({
	InitPersistenceUnit.class
})
public class AllPluginTests {
	
	/**
	 * @return an h2 based test database connection
	 */
	public static DBConnection getTestDatabaseConnection(){
		DBConnection retVal = new DBConnection();
		retVal.connectionString = "jdbc:h2:mem:elexisTest;DB_CLOSE_DELAY=-1";
		retVal.rdbmsType = DBType.H2;
		retVal.username = "sa";
		retVal.password = "";
		return retVal;
	}
}
