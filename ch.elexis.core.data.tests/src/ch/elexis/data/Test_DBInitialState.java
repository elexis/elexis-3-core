package ch.elexis.data;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.elexis.core.data.activator.CoreHub;
import ch.rgw.tools.JdbcLink;

public class Test_DBInitialState extends AbstractPersistentObjectTest {
	
	public Test_DBInitialState(JdbcLink link){
		super(link);
	}
	
	@Test
	public void testDatabaseUpdatedToRequiredVersion(){
		String requiredDb = CoreHub.globalCfg.get("dbversion", "wr0ng");
		assertEquals(CoreHub.DBVersion, requiredDb);
	}
}
