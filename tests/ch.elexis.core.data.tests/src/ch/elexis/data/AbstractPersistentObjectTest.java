package ch.elexis.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.DBConnection;
import ch.elexis.core.services.IElexisDataSource;
import ch.elexis.core.services.IElexisEntityManager;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.rgw.tools.JdbcLink;

@Ignore
@RunWith(Parameterized.class)
public class AbstractPersistentObjectTest {
	
	protected JdbcLink link;
	protected String testUserName;
	protected final String PASSWORD = "password";
	private static Collection<JdbcLink> connections = new ArrayList<JdbcLink>();
	private static IModelService modelService;
	private static IElexisEntityManager entityManager;
	private static String savedConnection = "";
	
	@Parameters(name = "{0}")
	public static Collection<JdbcLink> data() throws IOException{
		for(DBConnection dbConn : AllDataTests.getConnections()){
			JdbcLink link = new JdbcLink(dbConn.rdbmsType.driverName, dbConn.connectionString, dbConn.rdbmsType.dbType);
			assert(link.getConnectString().contentEquals(dbConn.connectionString));
			connections.add(link);
		}
		return connections;
	}
	
	public AbstractPersistentObjectTest(JdbcLink link){
		this.link = link;

		DBConnection dbConnection = null;
		try {
			for(DBConnection dbConn : AllDataTests.getConnections()){
				if (dbConn.rdbmsType.dbType.toLowerCase().contentEquals(link.DBFlavor.toLowerCase()))
				{
					dbConnection = dbConn;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assert(dbConnection != null);
		assertTrue(dbConnection.allValuesSet());
		
		// We initilize the NoPO (liquibase) and PO (jdbcLink) based database
		// each time we get a new connectionString
		assertEquals(dbConnection.connectionString, link.getConnectString());
		if (!savedConnection.contentEquals(dbConnection.connectionString)) {
			LoggerFactory.getLogger(this.getClass()).info("Do Connect for DataSource: " + dbConnection.connectionString);
			System.out.println("Do Connect for DataSource: " + dbConnection.connectionString);
			savedConnection = dbConnection.connectionString;

			entityManager = OsgiServiceUtil.getService(IElexisEntityManager.class).get();
			entityManager.getEntityManager();
			modelService = OsgiServiceUtil
					.getService(IModelService.class, "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)").get();
			IElexisDataSource elexisDataSource = OsgiServiceUtil.getService(IElexisDataSource.class).get();
			elexisDataSource.setDBConnection(dbConnection);
			link.connect(dbConnection.username, dbConnection.password);
			PersistentObject.connect(link);
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
	}
	
	public JdbcLink getLink(){
		return link;
	}
}
