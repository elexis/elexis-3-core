package ch.elexis.core.data.lab;

import org.apache.commons.lang.StringUtils;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.ILabItem;
import ch.elexis.core.types.Gender;
import ch.elexis.core.types.LabItemTyp;
import ch.elexis.core.types.PathologicDescription;
import ch.elexis.core.types.PathologicDescription.Description;
import ch.elexis.data.LabResult;

public class LabResultEvaluator {
	
	private LabResultEvaluationResult evaluateTextualResult(LabResult labResult,
		final boolean isAbsoluteItem){
		
		String lcResult = labResult.getResult().trim();
		Object[] ref = getReferenceValueForLabResult(labResult);
		String refValue = (String) ref[0];
		Description description =
			(isAbsoluteItem) ? Description.PATHO_ABSOLUT : (Description) ref[1];
		
		if (lcResult.equals(refValue)) {
			return new LabResultEvaluationResult(true, false,
				new PathologicDescription(description, lcResult));
		}
		
		if (isAbsoluteItem) {
			if (lcResult.toLowerCase().startsWith("pos")
				|| lcResult.toLowerCase().startsWith("+")) {
				return new LabResultEvaluationResult(true, true,
					new PathologicDescription(description, lcResult));
			}
		}
		
		String SELECTED_BASE =
			(isAbsoluteItem) ? Preferences.LABSETTINGS_CFG_EVAL_PREFIX_TYPE_ABSOLUT
					: Preferences.LABSETTINGS_CFG_EVAL_PREFIX_TYPE_TEXT;
		if (CoreHub.globalCfg.get(
			SELECTED_BASE
				+ Preferences.LABSETTINGS_CFG_EVAL_REFVAL_NON_EQUAL_RESVAL_MEANS_PATHOLOGIC,
			false)) {
	
			if (!lcResult.equalsIgnoreCase(refValue)) {
				return new LabResultEvaluationResult(true, true,
					new PathologicDescription(description, lcResult));
			}
		}
		
		return new LabResultEvaluationResult(true, false,
			new PathologicDescription(Description.UNKNOWN, lcResult));
	}
	
	private Object[] getReferenceValueForLabResult(LabResult labResult){
		Gender gender = labResult.getPatient().getGender();
		
		Description description = Description.PATHO_REF;
		String refValue = "";
		if (Gender.MALE == gender) {
			refValue = labResult.getRefMale();
			if (StringUtils.isEmpty(refValue)) {
				description = Description.PATHO_REF_ITEM;
				refValue = labResult.getItem().getReferenceMale();
			}
		} else {
			refValue = labResult.getRefFemale();
			if (StringUtils.isEmpty(refValue)) {
				description = Description.PATHO_REF_ITEM;
				refValue = labResult.getItem().getReferenceFemale();
			}
		}
		return new Object[] {
			refValue, description
		};
	}
	
	public LabResultEvaluationResult evaluate(LabResult labResult){
		ILabItem item = labResult.getItem();
		if(item.isNoReferenceValueItem()) {
			return new LabResultEvaluationResult(true, false, new PathologicDescription(
				Description.PATHO_REF_ITEM, Messages.LabResultEvaluator_LabItemNoRefValue));
		}
		if (LabItemTyp.ABSOLUTE == item.getTyp()) {
			return evaluateTextualResult(labResult, true);
		} else if (LabItemTyp.TEXT == item.getTyp()) {
			return evaluateTextualResult(labResult, false);
		} else if (LabItemTyp.DOCUMENT == item.getTyp()) {
			/** document is never pathologic **/
			return new LabResultEvaluationResult(true, false, null);
		}
		return new LabResultEvaluationResult(false);
	}
	
}
