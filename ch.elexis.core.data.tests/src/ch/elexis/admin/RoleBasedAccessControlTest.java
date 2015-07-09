package ch.elexis.admin;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.data.AbstractPersistentObjectTest;
import ch.elexis.data.Anwender;
import ch.elexis.data.Person;
import ch.elexis.data.Role;
import ch.elexis.data.User;

public class RoleBasedAccessControlTest extends AbstractPersistentObjectTest {
	
	private static final String USERNAME = "user";
	private static final String PASSWORD = "password";

	@BeforeClass
	public static void setUp() throws Exception {
		RoleBasedAccessControlTest.initDB();
		new Anwender(USERNAME, PASSWORD);
	}
	
	private User setContext(){
		boolean succ = Anwender.login(USERNAME, PASSWORD);
		assertTrue(succ);
		User user = (User) ElexisEventDispatcher.getSelected(User.class);
		assertNotNull(user);
		return user;
	}
	
	@Test
	public void testInitialState() {
		User user = User.load(User.USERNAME_ADMINISTRATOR);
		assertNotNull("User Administrator is null", user);
		Person assignedContact = user.getAssignedContact();
		assertNotNull("No contact assoaciated to user administrator", assignedContact);
		boolean queryRightForUser = CoreHub.acl.request(user, AccessControlDefaults.AC_LOGIN);
		assertTrue("Administrator is denied login", queryRightForUser);
	}
	
	@Test
	public void testUserAddWithOKRight() {
		User user = setContext();
		boolean rightOk = RoleBasedAccessControl.queryRightForUser(user, AccessControlDefaults.AC_EXIT);
		assertTrue(rightOk);
	}

	@Test
	public void testUserAddWithNonOKRight() {
		User user = setContext();
		boolean rightFalse = RoleBasedAccessControl.queryRightForUser(user, AccessControlDefaults.ADMIN_ACE);
		assertFalse(rightFalse);
	}
	
	@Test
	public void testUserAddAndRevokeParentRightInvolvesChildRights() {
		User user = setContext();
		Role userRole = Role.load(Role.SYSTEMROLE_LITERAL_USER);
		userRole.grantAccessRight(RoleBasedAccessControlTestACLContribution.parent);
		boolean rightTrue = RoleBasedAccessControl.queryRightForUser(user, RoleBasedAccessControlTestACLContribution.child1child1);
		assertTrue(rightTrue);
		userRole.revokeAccessRight(RoleBasedAccessControlTestACLContribution.parent);
		boolean rightFalse = RoleBasedAccessControl.queryRightForUser(user, RoleBasedAccessControlTestACLContribution.child1child1);
		assertFalse(rightFalse);
	}
	
	@Test
	public void testUserLock() {
		User user = setContext();
		user.setActive(false);
		CoreHub.logoffAnwender();
		boolean rightFalse = Anwender.login(USERNAME, PASSWORD);
		assertFalse(rightFalse);
		
		// activate user again
		user.setActive(true);
	}
}
