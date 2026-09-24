package ch.elexis.core.fhir.model.remote.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.eenv.AccessToken;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IElexisDataSource;
import ch.elexis.core.services.IUserService;

public class SystemStartupTest {

	@Test
	public void systemStartupContext() {
		IContextService contextService = PortableServiceLoader.get(IContextService.class);
		AccessToken accessToken = contextService.getTyped(AccessToken.class).get();
		assertEquals("unittest", accessToken.getUsername());
		IContact userContact = contextService.getActiveUserContact().get();
		assertNotNull(userContact.getId());

	}

	@Test
	public void dataSourceIsNull() {
		IElexisDataSource elexisDataSource = PortableServiceLoader.get(IElexisDataSource.class);
		assertNull(elexisDataSource.getCurrentConnectionStatus());
	}

	@Test
	public void userContext() {
		IUserService iUserService = PortableServiceLoader.get(IUserService.class);
		List<IMandator> allExecutiveDoctors = iUserService.findAllExecutiveDoctors();
		assertEquals(4, allExecutiveDoctors.size());
		assertEquals("myElexis Mandator (m), 15.04.1990", allExecutiveDoctors.get(0).getLabel());

		IUser user = PortableServiceLoader.get(IContextService.class).getActiveUser().orElseThrow();
		Set<IMandator> executiveDoctorsWorkingFor = iUserService.getExecutiveDoctorsWorkingFor(user);
		assertEquals(2, executiveDoctorsWorkingFor.size());
	}

}
