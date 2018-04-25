package ch.elexis.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ch.elexis.core.model.RoleConstants;
import ch.rgw.tools.JdbcLink;

public class Test_User extends AbstractPersistentObjectTest {
	
	public Test_User(JdbcLink link){
		super(link);
	}
	
	private static Anwender anwender;
	
	@BeforeClass
	public static void beforeClass(){
		anwender = new Anwender("Ann", "Wender", "15.10.1933", "w");
	}
	
	@Ignore
	public void testCaseSensitiveIdLoad(){
		//#5514
		Anwender anw = new Anwender("Username", "Uservorname", "16.1.1973", "w");
		new User(anw, "user", "pass");
		
		assertFalse(User.load("USER").exists());
		assertFalse(User.load("User").exists());
		assertTrue(User.load("user").exists());
	}
	
	@Test
	public void testCreateAndDeleteUser(){
		new User(anwender, "anw", "password");
		User user = User.load("anw");
		assertTrue(user.exists());
		Role userRole = Role.load(RoleConstants.SYSTEMROLE_LITERAL_USER);
		assertTrue(user.getAssignedRoles().contains(userRole));
		assertTrue(user.isActive());
		assertTrue(user.verifyPassword("password"));
		
		assertTrue(user.delete());
		assertFalse(user.exists());
		assertFalse(User.verifyUsernameNotTaken("anw"));
	}
	
}
