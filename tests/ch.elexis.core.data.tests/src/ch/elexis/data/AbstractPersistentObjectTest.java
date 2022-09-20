package ch.elexis.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;

import ch.elexis.core.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.extension.CoreOperationAdvisorHolder;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLink.Stm;
import ch.rgw.tools.JdbcLinkException;

@Ignore
public class AbstractPersistentObjectTest {

	private JdbcLink jdbcLink;

	protected String testUserName;
	protected final String PASSWORD = "password";

	public AbstractPersistentObjectTest() {
		assertNotNull(CoreHub.localCfg);
		jdbcLink = PersistentObject.getConnection();
		PersistentObject.clearCache();

		// reset the datasource
		User.initTables();

		if (testUserName == null) {
			testUserName = "ut_user_";
		}

		User existingUser = User.load(testUserName);
		if (!existingUser.exists()) {
			new Anwender(testUserName, PASSWORD);
			new Mandant("ut_mandator_", PASSWORD);
		}

		System.setProperty(ElexisSystemPropertyConstants.LOGIN_USERNAME, testUserName);
		System.setProperty(ElexisSystemPropertyConstants.LOGIN_PASSWORD, PASSWORD);

		boolean succ = CoreOperationAdvisorHolder.get().performLogin(null);
		assertTrue(succ);
	}

	public void executeStatement(String statement) {
		Stm stm = null;
		try {
			stm = jdbcLink.getStatement();
			stm.exec(statement);
		} catch (JdbcLinkException je) {
			je.printStackTrace();
		} finally {
			if (stm != null) {
				jdbcLink.releaseStatement(stm);
			}
		}
	}

	public JdbcLink getLink() {
		return jdbcLink;
	}
}
