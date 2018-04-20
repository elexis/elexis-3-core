package ch.elexis.core.ui.property;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.util.BriefExternUtil;

public class PropertyTester extends org.eclipse.core.expressions.PropertyTester {
	
	public PropertyTester(){
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue){
		if ("documenteditlocal".equals(property)) { //$NON-NLS-1$
			// no local copy / local edit if brief extern
			if (CoreHub.globalCfg.get(Preferences.P_TEXT_EXTERN_FILE, false)
				&& BriefExternUtil.isValidExternPath(
					CoreHub.globalCfg.get(Preferences.P_TEXT_EXTERN_FILE_PATH, null), false)) {
				return false;
			}
			return CoreHub.localCfg.get(Preferences.P_TEXT_EDIT_LOCAL, false);
		}
		return false;
	}
	
}
