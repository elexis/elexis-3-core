package ch.elexis.data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.rgw.tools.JdbcLink;

public class Test_DBInitialState extends AbstractPersistentObjectTest {
	
	public Test_DBInitialState(JdbcLink link){
		super(link, true);
	}
	
	@Test
	public void testDatabaseUpdatedToRequiredVersion(){
		// undo means refresh from database 
		CoreHub.globalCfg.undo();
		String requiredDb = ConfigServiceHolder.getGlobal("dbversion", "wr0ng");
		assertEquals("liquibase", requiredDb);
	}
}
