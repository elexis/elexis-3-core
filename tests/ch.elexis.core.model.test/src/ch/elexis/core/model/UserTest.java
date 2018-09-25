package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.builder.IUserBuilder;

public class UserTest extends AbstractTest {
	
	private IRole userRole;
	private IRole testRole;
	
	@Before
	public void before(){
		super.before();
		createPerson();
		
		userRole = modelService.create(IRole.class);
		userRole.setSystemRole(true);
		userRole.setId(RoleConstants.SYSTEMROLE_LITERAL_USER);
		modelService.save(userRole);
		
		testRole = modelService.create(IRole.class);
		testRole.setSystemRole(false);
		testRole.setId("testRole");
		modelService.save(testRole);
	}
	
	@After
	public void after(){
		removePerson();
		modelService.remove(userRole);
		super.after();
	}
	
	@Test
	public void createFindRemove(){
		new IUserBuilder(modelService, "test", person).buildAndSave();
		
		IUser user = modelService.load("test", IUser.class).get();
		assertEquals("test", user.getUsername());
		assertEquals(person.getId(), user.getAssignedContact().getId());
		assertTrue(user.getRoles()
			.contains(modelService.load(RoleConstants.SYSTEMROLE_LITERAL_USER, IRole.class).get()));
		assertTrue(user.isActive());
		
		modelService.remove(user);
		
		assertFalse(modelService.load("test", IUser.class).isPresent());
	}
	
	@Test
	public void addRemoveRole() {
		IUser user = new IUserBuilder(modelService, "test", person).buildAndSave();
		
		assertFalse(user.getRoles().contains(testRole));
		assertTrue(user.getRoles().contains(userRole));
		user.addRole(testRole);
		modelService.save(user);
		
		user = modelService.load(user.getId(), IUser.class).get();
		assertTrue(user.getRoles().contains(testRole));
		assertTrue(user.getRoles().contains(userRole));
		
		user.removeRole(testRole);
		modelService.save(user);
		
		user = modelService.load(user.getId(), IUser.class).get();
		assertFalse(user.getRoles().contains(testRole));
		assertTrue(user.getRoles().contains(userRole));
	}
	
}
