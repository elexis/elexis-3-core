package ch.elexis.data.activator;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.services.IExternalLoginService;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.elexis.data.AbstractPersistentObjectTest;
import ch.elexis.data.Anwender;
import ch.elexis.data.User;
import ch.elexis.data.service.internal.CsvLoginService;
import ch.rgw.tools.JdbcLink;

public class CoreHubTest extends AbstractPersistentObjectTest {
	
	public CoreHubTest(JdbcLink link){
		super(link);
	}
	
	@Test
	public void testLoginInternal(){
		Anwender anw = new Anwender("Username", "Uservorname", "16.1.1973", "w");
		new User(anw, "user", "pass");
		
		// login to internal db
		Assert.assertTrue(CoreHub.login("user", "pass".toCharArray()));
		Assert.assertFalse(CoreHub.login("user", "pass123".toCharArray()));
	}
	
	@Test
	public void testLoginExternal(){
		// a contact with ID AnwenderTestLoginID is needed
		Anwender anw = new Anwender("anwName", "anwVorname", "16.1.1973", "w");
		new User(anw, "user1", "pass1");
		
		// login to external
		Optional<IExternalLoginService> extService =
			OsgiServiceUtil.getService(IExternalLoginService.class);
		Assert.assertTrue(extService.isPresent());
		
		Assert.assertTrue(extService.get() instanceof CsvLoginService);
		((CsvLoginService) extService.get()).initCsvRows(anw.getId());
		
		Assert.assertTrue(CoreHub.login("sazgin1", "password".toCharArray()));
		Assert.assertFalse(CoreHub.login("sazgin1", "1234".toCharArray()));
		
		// login to external with invalid credentials - fallback to internal login
		Assert.assertTrue(CoreHub.login("user1", "pass1".toCharArray()));
		Assert.assertFalse(CoreHub.login("user1", "1234".toCharArray()));
	}
}
