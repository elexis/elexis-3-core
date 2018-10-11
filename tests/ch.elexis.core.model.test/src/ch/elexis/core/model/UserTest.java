package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.builder.IUserBuilder;
import ch.elexis.core.test.AbstractTest;

public class UserTest extends AbstractTest {
	
	private IRole userRole;
	private IRole testRole;
	
	@Before
	public void before(){
		super.before();
		createPerson();
		
		userRole = coreModelService.create(IRole.class);
		userRole.setSystemRole(true);
		userRole.setId(RoleConstants.SYSTEMROLE_LITERAL_USER);
		coreModelService.save(userRole);
		
		testRole = coreModelService.create(IRole.class);
		testRole.setSystemRole(false);
		testRole.setId("testRole");
		coreModelService.save(testRole);
	}
	
	@After
	public void after(){
		coreModelService.remove(userRole);
		super.after();
	}
	
	@Test
	public void createFindRemove(){
		new IUserBuilder(coreModelService, "test", person).buildAndSave();
		
		IUser user = coreModelService.load("test", IUser.class).get();
		assertEquals("test", user.getUsername());
		assertEquals(person.getId(), user.getAssignedContact().getId());
		assertTrue(user.getRoles()
			.contains(coreModelService.load(RoleConstants.SYSTEMROLE_LITERAL_USER, IRole.class).get()));
		assertTrue(user.isActive());
		
		coreModelService.remove(user);
		
		assertFalse(coreModelService.load("test", IUser.class).isPresent());
	}
	
	@Test
	public void addRemoveRole() {
		IUser user = new IUserBuilder(coreModelService, "test", person).buildAndSave();
		
		assertFalse(user.getRoles().contains(testRole));
		assertTrue(user.getRoles().contains(userRole));
		user.addRole(testRole);
		coreModelService.save(user);
		
		user = coreModelService.load(user.getId(), IUser.class).get();
		assertTrue(user.getRoles().contains(testRole));
		assertTrue(user.getRoles().contains(userRole));
		
		user.removeRole(testRole);
		coreModelService.save(user);
		
		user = coreModelService.load(user.getId(), IUser.class).get();
		assertFalse(user.getRoles().contains(testRole));
		assertTrue(user.getRoles().contains(userRole));
	}
	
}
