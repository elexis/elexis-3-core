package ch.elexis.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Optional;

import org.junit.Test;

import ch.elexis.core.model.IRole;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.RoleConstants;
import ch.elexis.core.test.TestEntities;
import ch.elexis.core.utils.OsgiServiceUtil;

public class UserServiceTest {

	private IUserService service = OsgiServiceUtil.getService(IUserService.class).get();

	@Test
	public void userLoadChangeVerifyPassword() {
		Optional<IUser> load = AllServiceTests.getModelService().load(TestEntities.USER_USER_ID, IUser.class);
		assertTrue(load.isPresent());
		assertNotNull(load.get().getHashedPassword());
		assertNotNull(load.get().getSalt());
		Collection<IRole> roles = load.get().getRoles();
		assertNotNull(roles);
		assertEquals(RoleConstants.SYSTEMROLE_LITERAL_USER, roles.iterator().next().getId());

		assertFalse(service.verifyPassword(load.get(), "invalid"));
		service.setPasswordForUser(load.get(), "password");
		assertTrue(service.verifyPassword(load.get(), "password"));

		Optional<IRole> userRole = AllServiceTests.getModelService().load(RoleConstants.SYSTEMROLE_LITERAL_USER,
				IRole.class);
		assertTrue(load.get().getRoles().contains(userRole.get()));
	}
}
