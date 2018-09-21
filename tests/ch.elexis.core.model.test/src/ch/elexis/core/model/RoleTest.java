package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.elexis.core.services.IQuery;

public class RoleTest extends AbstractTest {
	
	@Test
	public void createFindDelete(){
		IRole role = modelService.create(IRole.class);
		role.setId("testRole");
		role.setSystemRole(true);
		modelService.save(role);
		
		IQuery<IRole> query = modelService.getQuery(IRole.class);
		assertEquals(role, query.executeSingleResult().get());
	}
}
