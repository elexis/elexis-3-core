package ch.elexis.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
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
	
	@Test
	public void testApiKey(){
		User user2 = new User(anwender, "anw2", "password");
		String apiKey = RandomStringUtils.randomAlphanumeric(64);
		user2.set(User.FLD_APIKEY, apiKey);
		
		List<User> result = new Query<User>(User.class, User.FLD_APIKEY, apiKey).execute();
		assertThat(result, is(Arrays.asList(user2)));
	}
	
}
