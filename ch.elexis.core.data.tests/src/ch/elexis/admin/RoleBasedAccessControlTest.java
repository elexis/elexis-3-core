package ch.elexis.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.model.RoleConstants;
import ch.elexis.data.AbstractPersistentObjectTest;
import ch.elexis.data.Anwender;
import ch.elexis.data.Person;
import ch.elexis.data.Query;
import ch.elexis.data.Right;
import ch.elexis.data.Role;
import ch.elexis.data.User;
import ch.rgw.tools.JdbcLink;

public class RoleBasedAccessControlTest extends AbstractPersistentObjectTest {

	public RoleBasedAccessControlTest(JdbcLink link){
		super(link);
	}

	private User user;
	private boolean needs_dump = false;

	@Before
	public void before() throws Exception{
		user = (User) ElexisEventDispatcher.getSelected(User.class);
		assertNotNull(user);
	}

	private static void dump_rights(JdbcLink link)
	{
		PreparedStatement ps = link.getPreparedStatement(
				"SELECT id, name FROM right_ order by id");
			ResultSet res = null;
			try {
				res = ps.executeQuery();
				int index = 0;
				while (res.next()) {
					index ++;
					StringBuilder sb = new StringBuilder();
					ResultSetMetaData rsmd = res.getMetaData();
					int numberOfColumns = rsmd.getColumnCount();
					for (int i = 1; i <= numberOfColumns; i++) {
					    sb.append(res.getString(i));
					    if (i < numberOfColumns) {
					        sb.append(", ");
					    }
					}
					String data = sb.toString();
					System.out.println(index + ": " + data);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.out.println("catched " + e.getMessage());
				e.printStackTrace();
			}
	}
	@Test
	public void testInitialState(){
		User user = User.load(User.USERNAME_ADMINISTRATOR);
		assertNotNull("User Administrator is null", user);
		Person assignedContact = user.getAssignedContact();
		assertNotNull("No contact assoaciated to user administrator", assignedContact);
		boolean queryRightForUser = CoreHub.acl.request(user, AccessControlDefaults.AC_LOGIN);
		assertTrue("Administrator is denied login", queryRightForUser);
		List<Right> rights = new Query<Right>(Right.class).execute();
		/* Expected output of dump_rights(link) is
		 *
			1: 103a6569184c75dc, Verrechnen
			2: 11629a6a36aa66c2, Anwender
			3: 11d2973c44cdd88c3, Mandant
			4: 13d8771beb1b, Fall
			5: 15101375d93a134a, Systemvorlagen ändern
			6: 1567e02bbaf5e8c5, Laborwerte
			7: 1627eaa2f9d37bba, Anmelden
			8: 166c5fbe9a622f, Admin
			9: 1760d3cd767660, Daten
			10: 185e66bcd74ca01a, createCategory
			11: 199c713ed089b6a, Hilfe
			12: 19e98dde2b1a45f, Konsultation
			13: 1a463ca163308abe, Define_specials
			14: 1c18d2a479a16ccc, Laborparamter vereinen
			15: 296862557663050, createBills
			16: 371a57e8a0eb3, Über
			17: 44ef3f0a713e16, read
			18: 4b504fdd0f26593b, Vorlagen ändern
			19: 52fa258a537e15ab, change_billed
			20: 5c2f42e3bfab5a55, delete
			21: 5e8504baa3ccbb00, Kontakt
			22: 5e856339cbdd1b04, create
			23: 5f0b9757dcf1ee5, Patient
			24: 710051961b7bc441, Beenden
			25: 79d3a17fc74f5, copy
			26: 7e4592d8c3c785f, Konsultation
			27: 9797c8ed2540cd06, Specials
			28: 99a9748ac3d87e0, AlleVerrechnen
			29: 9d3df572d800dd93, LoadInfoStore
			30: 9e43a6d32fe6061, Rechnungen
			31: a8696d8a30864f87, Aktionen
			32: b373c30e3705866, Leistungen
			33: b57e2605144d75df, Reminders
			34: c19734cdf2ca2b8, AccountingGlobal
			35: c91c6da890975fa3, Zugriff
			36: ca488f5331595492, Dokumente
			37: dc5c457d83e95ed7, Löschen
			38: df86da724914fac, Dauermedikation		dump_rights(link);

			39: fd1b0cc8a25af715, Script
			40: root, root
		 */
		if (rights.size() != 40) {
			dump_rights(link);
			}
		System.out.println("Rights set: " +  rights.size() + " items\n"
			+ rights.stream().map(r -> r.getLabel()).collect(Collectors.joining(",")));
		assertEquals(40, rights.size());
		List<Role> roles = new Query<Role>(Role.class).execute();
		assertEquals(6, roles.size());
		Role ur = Role.load(RoleConstants.SYSTEMROLE_LITERAL_USER);
		ACE[] assignedUserRights = ur.getAssignedAccessRights();
		assertEquals(53, assignedUserRights.length);
		if (assignedUserRights.length != 53) {
			dump_rights(link);
			}
	}

	@Test
	public void testUserAddWithOKRight(){
		Role ur = Role.load(RoleConstants.SYSTEMROLE_LITERAL_USER);
		assertNotNull(ur);
		List<Role> assignedRoles = user.getAssignedRoles();
		boolean userHasRole = false;
		for (Role role : assignedRoles) {
			if (ur.getId().equals(role.getId())) {
				userHasRole = true;
			}
		}
		assertTrue(userHasRole);

		boolean roleHasRight =
			RoleBasedAccessControl.queryRightForRole(ur, AccessControlDefaults.AC_EXIT);
		assertTrue(roleHasRight);
		boolean userHasRight =
			RoleBasedAccessControl.queryRightForUser(user, AccessControlDefaults.AC_EXIT);
		assertTrue(userHasRight);
	}

	@Test
	public void testUserAddWithNonOKRight(){
		boolean rightFalse =
			RoleBasedAccessControl.queryRightForUser(user, AccessControlDefaults.ADMIN_ACE);
		assertFalse(rightFalse);
	}

	@Test
	public void testUserAddAndRevokeParentRightInvolvesChildRights(){
		Role userRole = Role.load(RoleConstants.SYSTEMROLE_LITERAL_USER);
		userRole.grantAccessRight(RoleBasedAccessControlTestACLContribution.parent);
		boolean rightTrue = RoleBasedAccessControl.queryRightForUser(user,
			RoleBasedAccessControlTestACLContribution.child1child1);
		assertTrue(rightTrue);
		userRole.revokeAccessRight(RoleBasedAccessControlTestACLContribution.parent);
		boolean rightFalse = RoleBasedAccessControl.queryRightForUser(user,
			RoleBasedAccessControlTestACLContribution.child1child1);
		assertFalse(rightFalse);
	}

	@Test
	public void testUserLock(){
		user.setActive(false);
		CoreHub.logoffAnwender();
		boolean rightFalse = Anwender.login(testUserName, PASSWORD);
		assertFalse(rightFalse);

		// activate user again
		user.setActive(true);
	}
}
