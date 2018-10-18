package ch.elexis.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.ac.AccessControlDefaults;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IRight;
import ch.elexis.core.model.IRole;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.RoleConstants;
import ch.elexis.core.services.internal.RoleBasedAccessControlTestACLContribution;
import ch.elexis.core.test.TestEntities;
import ch.elexis.core.utils.OsgiServiceUtil;

public class AccessControlServiceTest {
	
	private IModelService modelService = AllServiceTests.getModelService();
	private static IAccessControlService accessControlService;
	
	@BeforeClass
	public static void beforeClass(){
		accessControlService = OsgiServiceUtil.getService(IAccessControlService.class).get();
		accessControlService.initializeDefaults();
	}
	
	@Test
	public void testInitialState(){
		List<IRight> allSystemRights = modelService.getQuery(IRight.class).execute();
		System.out.println("Rights set: "
			+ allSystemRights.stream().map(r -> r.getLabel()).collect(Collectors.joining(",")));
		assertTrue(allSystemRights.size() >= 40);
		
		IUser user = modelService.load(TestEntities.USER_USER_ID, IUser.class).get();
		assertNotNull("User Administrator is null", user);
		IContact assignedContact = user.getAssignedContact();
		assertNotNull("No contact associated to user administrator", assignedContact);
		boolean queryRightForUser =
			accessControlService.request(user, AccessControlDefaults.AC_LOGIN);
		assertTrue("Administrator is denied login", queryRightForUser);
		List<IRole> userUserRoles = user.getRoles();
		assertEquals(2, userUserRoles.size());
		
		List<IRole> allSystemRoles = modelService.getQuery(IRole.class).execute();
		assertEquals(6, allSystemRoles.size());
		
		IRole userRole =
			modelService.load(RoleConstants.SYSTEMROLE_LITERAL_USER, IRole.class).get();
		List<IRight> userRoleAssignedRights = userRole.getAssignedRights();
		assertEquals(27, userRoleAssignedRights.size());
	}
	
	@Test
	public void testUserAddWithOKRight(){
		IUser user = modelService.load(TestEntities.USER_USER_ID, IUser.class).get();
		IRole ur = modelService.load(RoleConstants.SYSTEMROLE_LITERAL_USER, IRole.class).get();
		assertNotNull(ur);
		List<IRole> assignedRoles = user.getRoles();
		boolean userHasRole = false;
		for (IRole role : assignedRoles) {
			if (ur.getId().equals(role.getId())) {
				userHasRole = true;
			}
		}
		assertTrue(assignedRoles.toString(), userHasRole);
		
		boolean roleHasRight = accessControlService.request(ur, AccessControlDefaults.AC_EXIT);
		assertTrue(roleHasRight);
		boolean userHasRight = accessControlService.request(user, AccessControlDefaults.AC_EXIT);
		assertTrue(userHasRight);
	}
	
	@Test
	public void testUserAddWithNonOKRight(){
		IUser user = modelService.load(TestEntities.USER_USER_ID, IUser.class).get();
		boolean rightFalse = accessControlService.request(user, AccessControlDefaults.ADMIN_ACE);
		assertFalse(rightFalse);
	}
	
	@Test
	public void testUserAddAndRevokeParentRightInvolvesChildRights(){
		IUser user = modelService.load(TestEntities.USER_USER_ID, IUser.class).get();
		IRole userRole =
			modelService.load(RoleConstants.SYSTEMROLE_LITERAL_USER, IRole.class).get();
		accessControlService.grant(userRole, RoleBasedAccessControlTestACLContribution.parent);
		boolean rightTrue = accessControlService.request(user,
			RoleBasedAccessControlTestACLContribution.child1child1);
		assertTrue(rightTrue);
		accessControlService.revoke(userRole, RoleBasedAccessControlTestACLContribution.parent);
		boolean rightFalse = accessControlService.request(user,
			RoleBasedAccessControlTestACLContribution.child1child1);
		assertFalse(rightFalse);
	}
}
