package ch.elexis.core.services.internal;

import ch.elexis.core.ac.ACE;
import ch.elexis.core.ac.AbstractAccessControl;
import ch.elexis.core.ac.IACLContributor;

public class RoleBasedAccessControlTestACLContribution implements IACLContributor {
	
	public static final ACE parent = new ACE(ACE.ACE_ROOT, "testParent");
	public static final ACE child1 = new ACE( parent, "testChild1");
	public static final ACE child2 = new ACE( parent, "testChild2");
	public static final ACE child1child1 = new ACE( child1, "testChild1Child1");
	
	public RoleBasedAccessControlTestACLContribution(){}
	
	@Override
	public ACE[] getACL(){
		return new ACE[] {
			parent, child1, child2, child1child1
		};
	}
	
	@Override
	public void initializeDefaults(AbstractAccessControl ac){
		// TODO Auto-generated method stub
		
	}
	
}
