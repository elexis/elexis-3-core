package ch.elexis.data;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ch.elexis.core.common.DBConnection;
import ch.elexis.core.model.IConfig;
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
		
		// TODO: Howto correct initilize the NoPO (liquibase) and PO (jdbcLink) based database
		//   Niklaus does not know howto initialize the config table using liquibase 
		dbConnection.connectionString = link.getConnectString();
		link.connect(dbConnection.username, dbConnection.password);
		ch.elexis.data.DBConnection jdbcLinkPOconnection = new ch.elexis.data.DBConnection();
		jdbcLinkPOconnection.setJdbcLink(link);
		PersistentObjectUtil.initializeGlobalCfg(jdbcLinkPOconnection);
		PersistentObject.connect(link);
		IElexisDataSource elexisDataSource = OsgiServiceUtil.getService(IElexisDataSource.class).get();
		entityManager = OsgiServiceUtil.getService(IElexisEntityManager.class).get();
		elexisDataSource.setDBConnection(dbConnection);
		modelService = OsgiServiceUtil
				.getService(IModelService.class, "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)").get();
		entityManager = OsgiServiceUtil.getService(IElexisEntityManager.class).get();
		entityManager.getEntityManager(); // lazy initialize the database
		entityManager.getEntityManager(false);
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
	
	public JdbcLink getLink(){
		return link;
	}
}
