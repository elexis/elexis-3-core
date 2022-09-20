package ch.elexis.data;

import static org.junit.Assert.fail;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;

import ch.rgw.tools.JdbcLink.Stm;

public class Test_Trace extends AbstractPersistentObjectTest {

	@Test
	public void testAddTraceEntry() throws SQLException, InterruptedException {
		Trace.addTraceEntry("testUser", "testWorkstation", "testAction");
		// trace is written async

		for (int i = 0; i < 10; i++) {
			Stm statement = getLink().getStatement();
			ResultSet rs = statement.query("SELECT * FROM " + Trace.TABLENAME);
			while (rs.next()) {
				String string = rs.getString("ACTION");
				if ("testAction".equals(string)) {
					getLink().releaseStatement(statement);
					return;
				}
			}
			getLink().releaseStatement(statement);
			Thread.sleep(100);
		}
		fail();
	}

}
