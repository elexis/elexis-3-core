package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.elexis.core.services.IQuery;
import ch.elexis.core.test.AbstractTest;

public class RoleTest extends AbstractTest {
	
	@Test
	public void createFindDelete(){
		IRole role = coreModelService.create(IRole.class);
		role.setId("testRole");
		role.setSystemRole(true);
		coreModelService.save(role);
		
		IQuery<IRole> query = coreModelService.getQuery(IRole.class);
		assertEquals(role, query.executeSingleResult().get());
	}
}
