package ch.elexis.data;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ch.elexis.core.common.DBConnection;
import ch.elexis.core.common.DBConnection.DBType;
import ch.elexis.core.services.IElexisDataSource;
import ch.elexis.core.services.IElexisEntityManager;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.rgw.tools.JdbcLink;

@Ignore
@RunWith(Parameterized.class)
public class AbstractPersistentObjectTest {
	
	protected JdbcLink link;
	protected String testUserName;
	protected final String PASSWORD = "password";
	
	@Parameters(name = "{0}")
	public static Collection<JdbcLink[]> data() throws IOException{
		return AllDataTests.getConnections();
	}
	
	public AbstractPersistentObjectTest(JdbcLink link){
		this.link = link;

		// reset the datasource
		IElexisDataSource elexisDataSource = OsgiServiceUtil.getService(IElexisDataSource.class).get();
		DBConnection dbConnection = new DBConnection();
		dbConnection.databaseName = "unittests";
		dbConnection.username = "elexis";
		dbConnection.password = "elexisTest";

		switch (link.DBFlavor.toLowerCase()) {
		case "h2":
			dbConnection.rdbmsType = DBType.H2;
			dbConnection.databaseName = link.getConnectString().replace("jdbc:h2:", "");
			dbConnection.databaseName = link.getConnectString();
			dbConnection.username = "sa";
			dbConnection.password = "";
			break;
		case "mysql":
			dbConnection.rdbmsType = DBType.MySQL;
			break;
		case "postgresql":
			dbConnection.rdbmsType = DBType.PostgreSQL;
			break;
		default:
			System.out.println("Unrecognized DBFlavor " + link.DBFlavor);
		}
		ch.elexis.data.DBConnection dbConnection2 = new ch.elexis.data.DBConnection();
		dbConnection2.setJdbcLink(link);
		PersistentObject.connect(link);
		dbConnection.connectionString = link.getConnectString();
		elexisDataSource.setDBConnection(dbConnection);
		Optional<IElexisEntityManager> elexisEntityManager = OsgiServiceUtil.getService(IElexisEntityManager.class);
		elexisEntityManager.get().getEntityManager(false);
		User.initTables();
		
		if (testUserName == null) {
			testUserName = "ut_user_" + link.DBFlavor;
		}
		
		User existingUser = User.load(testUserName);
		if (!existingUser.exists()) {
			new Anwender(testUserName, PASSWORD);
			new Mandant("ut_mandator_" + link.DBFlavor, PASSWORD);
		}
		
		boolean succ = Anwender.login(testUserName, PASSWORD);
		assertTrue(succ);
	}
	
	public JdbcLink getLink(){
		return link;
	}
}
