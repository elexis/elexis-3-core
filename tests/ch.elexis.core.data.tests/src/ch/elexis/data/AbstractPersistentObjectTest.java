package ch.elexis.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ch.elexis.core.common.DBConnection;
import ch.elexis.core.common.DBConnection.DBType;
import ch.elexis.core.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.extension.CoreOperationAdvisorHolder;
import ch.elexis.core.services.IElexisDataSource;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLink.Stm;
import ch.rgw.tools.JdbcLinkException;

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
		this(link, false);
	}
	
	public AbstractPersistentObjectTest(JdbcLink link, boolean deleteTables){
		this.link = link;
		assertNotNull(link);
		assertNotNull(CoreHub.localCfg);
		if (deleteTables) {
			PersistentObject.connect(link);
			PersistentObject.deleteAllTables();
		}
		PersistentObject.clearCache();
		
		// reset the datasource
		IElexisDataSource elexisDataSource =
			OsgiServiceUtil.getService(IElexisDataSource.class).get();
		DBConnection dbConnection = new DBConnection();
		dbConnection.rdbmsType = DBType.valueOfIgnoreCase(link.DBFlavor).get();
		if (dbConnection.rdbmsType == DBType.H2) {
			dbConnection.username = "sa";
			dbConnection.password = "";
		} else if (dbConnection.rdbmsType == DBType.MySQL) {
			dbConnection.username = "elexisTest";
			dbConnection.password = "elexisTest";
		} else if (dbConnection.rdbmsType == DBType.PostgreSQL) {
			dbConnection.username = "elexistest";
			dbConnection.password = "elexisTest";
		}
		dbConnection.connectionString = link.getConnectString();
		elexisDataSource.setDBConnection(dbConnection);
		
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
		
		System.setProperty(ElexisSystemPropertyConstants.LOGIN_USERNAME, testUserName);
		System.setProperty(ElexisSystemPropertyConstants.LOGIN_PASSWORD, PASSWORD);
		
		boolean succ = CoreOperationAdvisorHolder.get().performLogin(null);
		assertTrue(succ);
	}
	
	public void executeStatement(String statement){
		Stm stm = null;
		try {
			stm = link.getStatement();
			stm.exec(statement);
		} catch (JdbcLinkException je) {
			je.printStackTrace();
		} finally {
			if (stm != null) {
				link.releaseStatement(stm);
			}
		}
	}
	
	public JdbcLink getLink(){
		return link;
	}
}
