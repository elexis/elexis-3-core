package ch.elexis.core.data.lab;

import org.apache.commons.lang.StringUtils;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.model.ILabItem;
import ch.elexis.core.types.Gender;
import ch.elexis.core.types.LabItemTyp;
import ch.elexis.core.types.PathologicDescription;
import ch.elexis.core.types.PathologicDescription.Description;
import ch.elexis.data.LabResult;

public class LabResultEvaluator {
	
	public static LabResultEvaluationResult evaluatePathologicItem(LabResult labResult){
		
		String lcResult = labResult.getResult().trim();
		String refValue = (String) getReferenceValueForLabResult(labResult)[0];
		
		if (lcResult.equals(refValue)) {
			return new LabResultEvaluationResult(true, false,
				new PathologicDescription(Description.PATHO_ABSOLUT, lcResult));
		}
		
		if (lcResult.toLowerCase().startsWith("pos") || lcResult.toLowerCase().startsWith("+")) {
			return new LabResultEvaluationResult(true, true,
				new PathologicDescription(Description.PATHO_ABSOLUT, lcResult));
		}
		
		if (CoreHub.globalCfg.get(
			Preferences.LABSETTINGS_CFG_EVAL_PREFIX_TYPE_ABSOLUT
				+ Preferences.LABSETTINGS_CFG_EVAL_REFVAL_NON_EQUAL_RESVAL_MEANS_PATHOLOGIC,
			false)) {
			
			if (!lcResult.equalsIgnoreCase(refValue)) {
				return new LabResultEvaluationResult(true, true,
					new PathologicDescription(Description.PATHO_ABSOLUT, lcResult));
			}
		}
		
		return new LabResultEvaluationResult(true, false,
			new PathologicDescription(Description.UNKNOWN, lcResult));
	}
	
	private LabResultEvaluationResult evaluateOtherItems(LabResult labResult){
		
		String lcResult = labResult.getResult().trim();
		Object[] refValue = getReferenceValueForLabResult(labResult);
		
		if (lcResult.equals((String) refValue[0])) {
			return new LabResultEvaluationResult(true, false,
				new PathologicDescription((Description) refValue[1], lcResult));
		}
		
		if (CoreHub.globalCfg.get(
			Preferences.LABSETTINGS_CFG_EVAL_PREFIX_TYPE_ABSOLUT
				+ Preferences.LABSETTINGS_CFG_EVAL_REFVAL_NON_EQUAL_RESVAL_MEANS_PATHOLOGIC,
			false)) {
			
			if (!lcResult.equalsIgnoreCase((String) refValue[0])) {
				return new LabResultEvaluationResult(true, true,
					new PathologicDescription((Description) refValue[1], lcResult));
			}
		}
		
		return new LabResultEvaluationResult(false, false,
			new PathologicDescription(Description.UNKNOWN, lcResult));
	}
	
	private static Object[] getReferenceValueForLabResult(LabResult labResult){
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
		if (LabItemTyp.ABSOLUTE == item.getTyp()) {
			return evaluatePathologicItem(labResult);
		} else if (LabItemTyp.DOCUMENT == item.getTyp()) {
			/** document is never pathologic **/
			return new LabResultEvaluationResult(true, false, null);
		} 
		return new LabResultEvaluationResult(false);
	}
	
}
