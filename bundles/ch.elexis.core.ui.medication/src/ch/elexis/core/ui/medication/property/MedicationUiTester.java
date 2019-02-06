package ch.elexis.core.ui.medication.property;

import java.util.Optional;

import org.eclipse.core.expressions.PropertyTester;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.services.holder.ContextServiceHolder;

public class MedicationUiTester extends PropertyTester {
	
	public static final String MEDICATION_SETTINGS_SHOW_CUSTOM_SORT =
		"medication/settings/showPersonalSort";
	
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue){
		if ("showCustomSort".equals(property)) { //$NON-NLS-1$
			return CoreHub.userCfg.get(MEDICATION_SETTINGS_SHOW_CUSTOM_SORT, false);
		} else if ("isDefaultArticle".equals(property)) {
			Optional<ch.elexis.core.model.IPrescription> prescription =
				ContextServiceHolder.get().getTyped(ch.elexis.core.model.IPrescription.class);
			if (prescription.isPresent()) {
				return !prescription.get().getArticle().getClass().getSimpleName()
					.contains("Artikelstamm");
			}
		}
		return false;
	}
}
