package ch.elexis.core.ui.property;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;

public class PropertyTester extends org.eclipse.core.expressions.PropertyTester {
	
	public PropertyTester(){
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue){
		if ("documenteditlocal".equals(property)) { //$NON-NLS-1$
			return CoreHub.localCfg.get(Preferences.P_TEXT_EDIT_LOCAL, false);
		}
		return false;
	}
	
}
