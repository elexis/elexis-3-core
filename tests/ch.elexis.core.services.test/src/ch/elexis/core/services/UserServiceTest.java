package ch.elexis.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IUser;
import ch.elexis.core.utils.OsgiServiceUtil;

public class UserServiceTest extends AbstractServiceTest {
	
	private IUserService service = OsgiServiceUtil.getService(IUserService.class).get();
	
	@Test
	public void getUsersByAssociatedContact(){
		// from ch.elexis.core.test/rsc/dbScript/User.sql
		IUser user = coreModelService.load("user", IUser.class).orElse(null);
		assertNotNull(user);
		IContact assocatiedContact =
			coreModelService.load("h2c1172107ce2df95065", IContact.class).orElse(null);
		assertNotNull(assocatiedContact);
		
		List<IUser> usersByAssociatedContact =
			service.getUsersByAssociatedContact(assocatiedContact);
		System.out.println(usersByAssociatedContact);
		assertEquals(user, usersByAssociatedContact.get(0));
		assertEquals(1, usersByAssociatedContact.size());
	}
	
}
