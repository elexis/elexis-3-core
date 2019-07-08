package ch.elexis.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.elexis.admin.RoleBasedAccessControlTest;
import ch.elexis.core.common.DBConnection;
import ch.elexis.core.common.DBConnection.DBType;
import ch.elexis.core.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.data.service.internal.BriefDocumentStoreTest;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLinkException;

@RunWith(Suite.class)
@SuiteClasses({ Test_DBInitialState.class, Test_PersistentObject.class, Test_Prescription.class,
		 Test_Patient.class, Test_LabItem.class, Test_DBImage.class, Test_Query.class,
		 Test_Verrechnet.class, Test_Reminder.class, Test_StockService.class,
		 Test_OrderService.class, Test_Konsultation.class,
		 RoleBasedAccessControlTest.class, Test_VkPreise.class,
		 Test_ZusatzAdresse.class, BriefDocumentStoreTest.class, Test_Rechnung.class,
		 Test_User.class, Test_LabResult.class, Test_BezugsKontakt.class
})
public class AllDataTests {

	private static Collection<DBConnection> connections = new ArrayList<DBConnection>();

	private static void rmDemoDb(String msg) {
		try {
			String demoDBLocation = System.getProperty(ElexisSystemPropertyConstants.DEMO_DB_LOCATION);
			if (demoDBLocation == null) {
				demoDBLocation = CoreHub.getWritableUserDir() + File.separator + "demoDB";
			}
			File demo = new File(demoDBLocation);
			if (demo.exists() && demo.isDirectory()) {
				System.out.println(msg);
				Files.walk(demo.toPath()).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
			}
		} catch (IOException e) {
			System.err.println("Error deleting demoDB. " + msg);
		}
	}

	public static final boolean PERFORM_UPDATE_TESTS = false;

	@BeforeClass
	public static void beforeClass() {
		System.err.println("BeforeClass ");
		rmDemoDb("AllDataTests: Deleting demoDB in beforeClass");
	}

	private static DBConnection initDbConnection(DBType flavor) {
		DBConnection dbConnection = new DBConnection();
		dbConnection.databaseName = "unittests";
		dbConnection.username = "elexisTest";
		dbConnection.password = "elexisTest";
		dbConnection.hostName = "localhost";
		dbConnection.rdbmsType = flavor;

		switch (flavor.dbType.toLowerCase()) {
		case "h2":
			dbConnection.databaseName = "~/elexis/elexisTest;AUTO_SERVER=TRUE";
			dbConnection.connectionString = dbConnection.username = "sa";
			dbConnection.password = "";
			dbConnection.hostName = "";
			dbConnection.connectionString = "jdbc:" + flavor.dbType.toLowerCase() + ":" + dbConnection.databaseName;
			break;
		case "mysql":
			dbConnection.username = "elexisTest";
			dbConnection.connectionString = "jdbc:" + flavor.dbType.toLowerCase() + "://" + dbConnection.hostName + "/"
					+ dbConnection.databaseName;
			break;
		case "postgresql":
			dbConnection.username = "elexistest";
			dbConnection.connectionString = "jdbc:" + flavor.dbType.toLowerCase() + "://" + dbConnection.hostName + "/"
					+ dbConnection.databaseName;
			break;
		default:
			System.out.println("Unrecognized DBFlavor " + flavor);
			assertTrue(false);
		}
		assertTrue(dbConnection.allValuesSet());
		return dbConnection;
	}

	static {
		DBConnection h2Db = initDbConnection(DBType.H2);
		DBConnection mysql2Db = initDbConnection(DBType.MySQL);
		DBConnection pgDb = initDbConnection(DBType.PostgreSQL);
		assertNotNull(h2Db);
		if (pgDb != null) {
			AllDataTests.connections.add(pgDb);
		}
		if (h2Db != null) {
			AllDataTests.connections.add(h2Db);
		}
		if (mysql2Db != null) {
			AllDataTests.connections.add(mysql2Db);
		}
	}

	@AfterClass
	public static void AfterClass() {
		for (DBConnection dbConn : AllDataTests.connections) {
			JdbcLink link = new JdbcLink(dbConn.rdbmsType.driverName, dbConn.connectionString, dbConn.rdbmsType.dbType);
			assert (link.getConnectString().contentEquals(dbConn.connectionString));
			try {
				PersistentObject.connect(link);
				PersistentObject.deleteAllTables();
				link.disconnect();
			} catch (JdbcLinkException je) {
				// just tell what happened and resume
				// exception is allowed for tests which get rid of the connection on their own
				// for example testConnect(), ...
				je.printStackTrace();
			}
		}
	}

	public static Collection<DBConnection> getConnections() throws IOException {
		assertTrue(connections.size() > 0);
		return connections;
	}
}