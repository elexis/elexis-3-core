package ch.elexis.core.model.builder;

import java.util.Optional;

import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IRole;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.RoleConstants;
import ch.elexis.core.services.IModelService;

public class IUserBuilder extends AbstractBuilder<IUser> {
	
	public IUserBuilder(IModelService modelService, String userId, IPerson contact){
		super(modelService);
		
		object = modelService.create(IUser.class);
		object.setUsername(userId);
		object.setAssignedContact(contact);
		object.setActive(true);
		object.setSalt("invalid");
		object.setHashedPassword("invalid");
		
		Optional<IRole> role = modelService.load(RoleConstants.SYSTEMROLE_LITERAL_USER, IRole.class);
		object.addRole(role.get());
	}
	
}
