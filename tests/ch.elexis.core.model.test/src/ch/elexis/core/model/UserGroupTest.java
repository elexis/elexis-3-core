package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.builder.IUserBuilder;
import ch.elexis.core.model.builder.IUserGroupBuilder;
import ch.elexis.core.services.INativeQuery;
import ch.elexis.core.test.AbstractTest;

public class UserGroupTest extends AbstractTest {

	private IUser userOne;
	private IUser userTwo;

	private IRole userRole;
	private IRole testRole;

	@Override
	@Before
	public void before() {
		super.before();
		super.createPerson();

		userRole = coreModelService.create(IRole.class);
		userRole.setSystemRole(true);
		userRole.setId(RoleConstants.ACCESSCONTROLE_ROLE_USER);
		coreModelService.save(userRole);

		testRole = coreModelService.create(IRole.class);
		testRole.setSystemRole(false);
		testRole.setId("testRole");
		coreModelService.save(testRole);

		userOne = new IUserBuilder(coreModelService, "userOne", person).buildAndSave();
		userTwo = new IUserBuilder(coreModelService, "userTwo", person).build();
		userTwo.addRole(testRole);
		coreModelService.save(userTwo);
	}

	@Override
	@After
	public void after() {
		coreModelService.remove(userRole);
		coreModelService.remove(testRole);
		coreModelService.remove(userOne);
		coreModelService.remove(userTwo);
		super.after();
	}

	@Test
	public void createFindRemove() {
		new IUserGroupBuilder(coreModelService, "test").buildAndSave();
		IUserGroup userGroup = coreModelService.load("test", IUserGroup.class).get();

		assertEquals("test", userGroup.getGroupname());
		assertTrue(userGroup.getRoles()
				.contains(coreModelService.load(RoleConstants.ACCESSCONTROLE_ROLE_USER, IRole.class).get()));

		coreModelService.remove(userGroup);

		assertFalse(coreModelService.load("test", IUserGroup.class).isPresent());
	}

	@Test
	public void query() {
		IUserGroup userGroup = new IUserGroupBuilder(coreModelService, "test").buildAndSave();
		userGroup.addUser(userOne);
		coreModelService.save(userGroup);

		INativeQuery nativeQuery = coreModelService
				.getNativeQuery("SELECT USERGROUP_ID FROM USERGROUP_USER_JOINT WHERE ID = ?1");
		Iterator<?> result = nativeQuery
				.executeWithParameters(nativeQuery.getIndexedParameterMap(Integer.valueOf(1), userOne.getId()))
				.iterator();
		assertTrue(result.hasNext());

		result = nativeQuery
				.executeWithParameters(nativeQuery.getIndexedParameterMap(Integer.valueOf(1), userTwo.getId()))
				.iterator();
		assertFalse(result.hasNext());
		coreModelService.remove(userGroup);
	}

	@Test
	public void addRemoveUser() {
		IUserGroup userGroup = new IUserGroupBuilder(coreModelService, "test").buildAndSave();

		assertFalse(userGroup.getUsers().contains(userOne));
		assertFalse(userGroup.getUsers().contains(userTwo));
		userGroup.addUser(userOne);
		coreModelService.save(userGroup);

		userGroup = coreModelService.load(userGroup.getId(), IUserGroup.class).get();
		assertTrue(userGroup.getUsers().contains(userOne));
		assertFalse(userGroup.getUsers().contains(userTwo));

		userGroup.removeUser(userOne);
		coreModelService.save(userGroup);

		userGroup = coreModelService.load(userGroup.getId(), IUserGroup.class).get();
		assertFalse(userGroup.getUsers().contains(userOne));
		assertFalse(userGroup.getUsers().contains(userTwo));
		coreModelService.remove(userGroup);
	}

	@Test
	public void addRemoveRole() {
		IUserGroup userGroup = new IUserGroupBuilder(coreModelService, "test").buildAndSave();

		assertFalse(userGroup.getRoles().contains(testRole));
		assertTrue(userGroup.getRoles().contains(userRole));
		userGroup.addRole(testRole);
		coreModelService.save(userGroup);

		userGroup = coreModelService.load(userGroup.getId(), IUserGroup.class).get();
		assertTrue(userGroup.getRoles().contains(testRole));
		assertTrue(userGroup.getRoles().contains(userRole));

		userGroup.removeRole(testRole);
		coreModelService.save(userGroup);

		userGroup = coreModelService.load(userGroup.getId(), IUserGroup.class).get();
		assertFalse(userGroup.getRoles().contains(testRole));
		assertTrue(userGroup.getRoles().contains(userRole));
		coreModelService.remove(userGroup);
	}
}
