package ch.elexis.core.model.builder;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IRole;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.RoleConstants;
import ch.elexis.core.services.IModelService;

public class IUserBuilder extends AbstractBuilder<IUser> {

	public IUserBuilder(IModelService modelService, String userId, IContact contact) {
		super(modelService);

		object = modelService.create(IUser.class);
		object.setUsername(userId);
		object.setAssignedContact(contact);
		object.setActive(true);
		object.setSalt("invalid");
		object.setHashedPassword("invalid");

		modelService.load(RoleConstants.ACCESSCONTROLE_ROLE_USER, IRole.class).ifPresent(object::addRole);
	}

}
