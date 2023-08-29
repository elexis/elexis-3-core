package ch.elexis.core.model.builder;

import ch.elexis.core.model.IRole;
import ch.elexis.core.model.IUserGroup;
import ch.elexis.core.model.RoleConstants;
import ch.elexis.core.services.IModelService;

public class IUserGroupBuilder extends AbstractBuilder<IUserGroup> {

	public IUserGroupBuilder(IModelService modelService, String userGroupId) {
		super(modelService);

		object = modelService.create(IUserGroup.class);
		object.setGroupname(userGroupId);

		modelService.load(RoleConstants.ACCESSCONTROLE_ROLE_USER, IRole.class).ifPresent(object::addRole);
	}
}
