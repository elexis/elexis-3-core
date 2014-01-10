package ch.elexis.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.exceptions.PersistenceException;
import ch.rgw.tools.JdbcLink;

public class Test_Query extends AbstractPersistentObjectTest {
	
	private JdbcLink link;

	@Before
	public void setUp() throws Exception{
		if (link != null) {
			PersistentObject.deleteAllTables();
			link.disconnect();
		}
		link = initDB();
		// create a instance of an PersistentObject ex. Organisation to test the query
		new Organisation("orgname", "orgzusatz1");
	}
	
	@After
	public void tearDown() throws Exception{
		PersistentObject.deleteAllTables();
		link.exec("DROP ALL OBJECTS");
		link.disconnect();
	}
	
	@Test
	public void testConstructor(){
		Query<Organisation> query = new Query<Organisation>(Organisation.class);
		// clear will access the template which is set in constructor ...
		// if it does not fail with an exception the constructor worked ...
		query.clear();
		
		query = new Query<Organisation>(Organisation.class, Organisation.FLD_NAME1, "orgname");
		// clear will access the template which is set in constructor ...
		// if it does not fail with an exception the constructor worked ...
		query.clear();
	}
	
	@Test
	public void testConstructorFail(){
		try {
			new Query<PersistentObjectImpl>(PersistentObjectImpl.class);
			fail("Expected Exception not thrown!");
		} catch (PersistenceException pe) {
			
		}
		
		try {
			new Query<PersistentObjectImpl>(PersistentObjectImpl.class, "", "");
			fail("Expected Exception not thrown!");
		} catch (PersistenceException pe) {
			
		}
	}
	@Test
	public void testGetPreparedStatement(){
		// fail("Not yet implemented"); // TODO
	}
	
	@Test
	public void testExecutePreparedStatementStringArray(){
		// fail("Not yet implemented"); // TODO
	}
	
	@Test
	public void testOrderBy(){
		// fail("Not yet implemented"); // TODO
	}
	
	@Test
	public void testExecute(){
		Query<Organisation> query = new Query<Organisation>(Organisation.class);
		query.clear();
		query.add(Organisation.FLD_NAME1, "=", "orgname");
		List<Organisation> result = query.execute();
		assertEquals(1, result.size());
	}
	
	@Test
	public void testQueryExpression(){
		PreparedStatement ps =
			link.prepareStatement("SELECT " + Organisation.FLD_NAME1 + " FROM "
				+ Organisation.TABLENAME);
		Query<Organisation> query = new Query<Organisation>(Organisation.class);
		ArrayList<String> result = query.execute(ps, new String[0]);
		System.out.println("found " + result.size() + " Organisation");
		for (String s : result) {
			System.out.println("Organisation: found " + s);
		}
		assertEquals(3, result.size());
	}
	
	private class PersistentObjectImpl extends PersistentObject {
		
		@SuppressWarnings("unused")
		public String getTestGet(){
			return "test";
		}
		
		@Override
		public String getLabel(){
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		protected String getTableName(){
			return null;
		}
		
	}
}
