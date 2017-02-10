package ch.elexis.data;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ch.rgw.tools.JdbcLink;

@RunWith(Parameterized.class)
public class AbstractPersistentObjectTest {
	
	private static final String USERNAME = "user";
	private static final String PASSWORD = "password";
	
	@Parameters
	public static Collection<Object[]> data() throws IOException{
		return AllTests.getConnections();
	}
	
	protected JdbcLink link;
	protected Anwender anwender;
	
	public AbstractPersistentObjectTest(JdbcLink link){
		this.link = link;
		PersistentObject.connect(link);
		
		User load = User.load(USERNAME);
		if (load != null && load.exists()) {
			anwender = load.getAssignedContact();
		} else {
			anwender = new Anwender(USERNAME, PASSWORD);
		}
		boolean succ = Anwender.login(USERNAME, PASSWORD);
		assertTrue(succ);
	}
	
	public JdbcLink getLink(){
		return link;
	}
}
