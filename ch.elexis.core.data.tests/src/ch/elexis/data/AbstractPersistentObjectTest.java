package ch.elexis.data;

import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ch.rgw.tools.JdbcLink;

@RunWith(Parameterized.class)
public class AbstractPersistentObjectTest {
	
	@Parameters
	public static Collection<Object[]> data(){
		return AllTests.getConnections();
	}
	
	protected JdbcLink link;
	
	public AbstractPersistentObjectTest(JdbcLink link){
		this.link = link;
		PersistentObject.connect(link);
	}
	
	public JdbcLink getLink(){
		return link;
	}
}
