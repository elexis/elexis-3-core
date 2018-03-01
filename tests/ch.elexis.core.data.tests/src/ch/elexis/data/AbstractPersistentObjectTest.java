package ch.elexis.data;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

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
