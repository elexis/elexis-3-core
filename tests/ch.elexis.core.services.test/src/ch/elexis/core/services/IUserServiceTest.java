package ch.elexis.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IRole;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.RoleConstants;
import ch.elexis.core.test.TestEntities;
import ch.elexis.core.utils.OsgiServiceUtil;

public class IUserServiceTest extends AbstractServiceTest {

	private IUserService service = OsgiServiceUtil.getService(IUserService.class).get();

	private IUser user;

	@Before
	public void before() {
		createTestMandantPatientFallBehandlung();
		user = coreModelService.load(TestEntities.USER_USER_ID, IUser.class).orElse(null);
		user.getAssignedContact().setExtInfo("StdMandant", testMandators.get(0).getId());
		user.getAssignedContact().setExtInfo("Mandant", testMandators.get(0).getLabel());
		coreModelService.save(user.getAssignedContact());
	}

	@After
	public void after() {
		cleanup();
	}

	@Test
	public void userLoadChangeVerifyPassword() {
		assertNotNull(user.getHashedPassword());
		assertNotNull(user.getSalt());
		Collection<IRole> roles = user.getRoles();
		assertNotNull(roles);
		assertEquals(RoleConstants.ACCESSCONTROLE_ROLE_USER, roles.iterator().next().getId());

		assertFalse(service.verifyPassword(user, "invalid".toCharArray()));
		service.setPasswordForUser(user, "password");
		assertTrue(service.verifyPassword(user, "password".toCharArray()));

		Optional<IRole> userRole = coreModelService.load(RoleConstants.ACCESSCONTROLE_ROLE_USER, IRole.class);
		assertTrue(user.getRoles().contains(userRole.get()));
	}

	@Test
	public void getExecutiveDoctorsWorkingFor() {
		Set<IMandator> executiveDoctorsWorkingFor = service.getExecutiveDoctorsWorkingFor(user);
		assertEquals(testMandators.get(0), executiveDoctorsWorkingFor.iterator().next());
	}

	@Test
	public void getDefaultExecutiveDoctorWorkingFor() {
		Optional<IMandator> defaultExecutiveDoctorWorkingFor = service
				.getDefaultExecutiveDoctorWorkingFor(user);
		assertEquals(testMandators.get(0), defaultExecutiveDoctorWorkingFor.get());
	}

}
