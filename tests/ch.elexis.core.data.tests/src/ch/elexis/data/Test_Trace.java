package ch.elexis.data;

import static org.junit.Assert.fail;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;

import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLink.Stm;

public class Test_Trace extends AbstractPersistentObjectTest {
	
	public Test_Trace(JdbcLink link){
		super(link);
	}
	
	@Test
	public void testAddTraceEntry() throws SQLException{
		Trace.addTraceEntry("testUser", "testWorkstation", "testAction");
		
		Stm statement = link.getStatement();
		ResultSet rs = statement
			.query("SELECT * FROM " + Trace.TABLENAME);
		while(rs.next()) {
			String string = rs.getString("ACTION");
			if("testAction".equals(string)) {
				link.releaseStatement(statement);
				return;
			}
		}
		link.releaseStatement(statement);
		fail();
	}
	
}
