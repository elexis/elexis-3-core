package ch.elexis.core.ui.medication.property;

import org.eclipse.core.expressions.PropertyTester;

import ch.elexis.core.data.activator.CoreHub;

public class MedicationUiTester extends PropertyTester {
	
	public static final String MEDICATION_SETTINGS_SHOW_CUSTOM_SORT =
		"medication/settings/showPersonalSort";
	
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue){
		if ("showCustomSort".equals(property)) { //$NON-NLS-1$
			return CoreHub.userCfg
				.get(MEDICATION_SETTINGS_SHOW_CUSTOM_SORT,
				false);
		}
		return false;
	}
}
