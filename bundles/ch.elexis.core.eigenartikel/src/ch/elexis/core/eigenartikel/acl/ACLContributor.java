package ch.elexis.core.eigenartikel.acl;

import ch.elexis.admin.ACE;
import ch.elexis.admin.AbstractAccessControl;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.admin.IACLContributor;
import ch.elexis.core.model.RoleConstants;

public class ACLContributor implements IACLContributor {
	
	public static final ACE EIGENARTIKEL_MODIFY = new ACE(AccessControlDefaults.DATA,
		"ch.elexis.core.eigenartikel.Eigenartikel.modify", "Eigenartikel bearbeiten");
	
	public ACLContributor(){}
	
	@Override
	public ACE[] getACL(){
		return new ACE[] {
			EIGENARTIKEL_MODIFY
		};
	}
	
	@Override
	public void initializeDefaults(AbstractAccessControl ac){
		ac.grant(RoleConstants.SYSTEMROLE_LITERAL_EXECUTIVE_DOCTOR, EIGENARTIKEL_MODIFY);
	}
	
}
