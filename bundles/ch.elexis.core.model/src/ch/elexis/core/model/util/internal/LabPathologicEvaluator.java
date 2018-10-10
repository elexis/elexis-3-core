package ch.elexis.core.model.util.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.ILabItem;
import ch.elexis.core.model.ILabResult;
import ch.elexis.core.types.Gender;
import ch.elexis.core.types.LabItemTyp;
import ch.elexis.core.types.PathologicDescription;
import ch.elexis.core.types.PathologicDescription.Description;

public class LabPathologicEvaluator {
	
	private static Pattern refValuesPattern = Pattern.compile("\\((.*?)\\)"); //$NON-NLS-1$
	
	private final String SMALLER = "<";
	private final String BIGGER = ">";
	
	/**
	 * Denotes that a {@link ILabItem} does not have a reference value, and hence by definition
	 * always is considered non-pathologic (e.g. eye color - blue)
	 */
	public static final String REFVAL_INCONCLUSIVE = "inconclusive";
	
	public static class EvaluationResult {
		/**
		 * the final state has been determined by the evaluator, no further evaluation required
		 */
		private boolean finallyDetermined;
		private boolean isPathologic;
		private PathologicDescription pathologicDescription;
		
		public EvaluationResult(boolean finallyDetermined){
			this.finallyDetermined = finallyDetermined;
		}
		
		public EvaluationResult(boolean finallyDetermined, boolean isPathologic,
			PathologicDescription pathologicDescription){
			this.finallyDetermined = finallyDetermined;
			this.isPathologic = isPathologic;
			this.pathologicDescription = pathologicDescription;
		}
		
		public boolean isFinallyDetermined(){
			return finallyDetermined;
		}
		
		public PathologicDescription getPathologicDescription(){
			return pathologicDescription;
		}
		
		public boolean isPathologic(){
			return isPathologic;
		}
	}
	
	public EvaluationResult evaluate(ILabResult labResult){
		ILabItem item = labResult.getItem();
		if (isNoReferenceValueItem(item)) {
			return new EvaluationResult(true, false, new PathologicDescription(
				Description.PATHO_REF_ITEM, Messages.LabResultEvaluator_LabItemNoRefValue));
		}
		if (LabItemTyp.ABSOLUTE == item.getTyp()) {
			return evaluateTextualResult(labResult, true);
		} else if (LabItemTyp.TEXT == item.getTyp()) {
			return evaluateTextualResult(labResult, false);
		} else if (LabItemTyp.DOCUMENT == item.getTyp()) {
			/** document is never pathologic **/
			return new EvaluationResult(true, false, null);
		}
		return new EvaluationResult(false);
	}
	
	/**
	 * Test if the {@link ILabResult} is pathologic, and update the pathologic value and
	 * {@link PathologicDescription} of the {@link ILabResult}.
	 * 
	 * @param labResult
	 * @return
	 */
	public boolean isPathologic(ILabResult labResult){
		return isPathologic(labResult, true);
	}
	
	/**
	 * Test if the {@link ILabResult} is pathologic. With the updateDescription parameter is
	 * specified whether the pathologic value and {@link PathologicDescription} should be updated.
	 * 
	 * @param labResult
	 * @param updateDescription
	 * @return
	 */
	public boolean isPathologic(ILabResult labResult, boolean updateDescription){
		if (labResult.getResult() == null) {
			return false;
		}
		EvaluationResult er = evaluate(labResult);
		if (er.isFinallyDetermined()) {
			if (updateDescription && er.getPathologicDescription() != null) {
				labResult.setPathologicDescription(er.getPathologicDescription());
			}
			return er.isPathologic();
		}
		
		String nr;
		boolean usedItemRef = false;
		if (labResult.getPatient().getGender() == Gender.MALE) {
			nr = labResult.getReferenceMale();
			usedItemRef = isUsingItemRef(labResult, Gender.MALE);
		} else {
			nr = labResult.getReferenceFemale();
			usedItemRef = isUsingItemRef(labResult, Gender.FEMALE);
		}
		List<String> refStrings = parseRefString(nr);
		// only test first string as range is defined in one string
		if (labResult.getResult() != null && !refStrings.isEmpty()
			&& !refStrings.get(0).isEmpty()) {
			if (updateDescription) {
				if (usedItemRef) {
					labResult.setPathologicDescription(
						new PathologicDescription(Description.PATHO_REF_ITEM, refStrings.get(0)));
				} else {
					labResult.setPathologicDescription(
						new PathologicDescription(Description.PATHO_REF, refStrings.get(0)));
				}
			}
			Boolean testResult = testRef(refStrings.get(0), labResult.getResult());
			if (testResult != null) {
				return testResult;
			} else {
				if (updateDescription) {
					labResult.setPathologicDescription(
						new PathologicDescription(Description.PATHO_NOREF, refStrings.get(0)));
				}
				return false;
			}
		}
		
		if (updateDescription) {
			labResult.setPathologicDescription(new PathologicDescription(Description.PATHO_NOREF));
		}
		return false;
	}
	
	/**
	 * Test if we use the reference value from the result or the item on
	 * {@link ILabResult#resolvePreferedRefValue(String, String)}.
	 * 
	 * @param gender
	 * @return
	 */
	private boolean isUsingItemRef(ILabResult labResult, Gender gender){
		boolean useLocalRefs = ModelUtil.isUserConfig(ModelUtil.getActiveUserContact().orElse(null),
			Preferences.LABSETTINGS_CFG_LOCAL_REFVALUES, true);
		String localRef = "";
		String ref = "";
		if (gender == Gender.MALE) {
			localRef = labResult.getItem().getReferenceMale();
			ref = labResult.getReferenceMale();
		} else {
			localRef = labResult.getItem().getReferenceFemale();
			ref = labResult.getReferenceFemale();
		}
		
		if (useLocalRefs && localRef != null && !localRef.isEmpty()) {
			return true;
		} else {
			if (ref == null || ref.isEmpty()) {
				return true;
			}
			return false;
		}
	}
	
	private boolean isNoReferenceValueItem(ILabItem item){
		return (REFVAL_INCONCLUSIVE.equals(item.getReferenceMale())
			&& REFVAL_INCONCLUSIVE.equals(item.getReferenceFemale()));
	}
	
	private Object[] getReferenceValueForLabResult(ILabResult labResult){
		Gender gender = labResult.getPatient().getGender();
		
		Description description = Description.PATHO_REF;
		String refValue = "";
		if (Gender.MALE == gender) {
			refValue = labResult.getReferenceMale();
			if (StringUtils.isEmpty(refValue)) {
				description = Description.PATHO_REF_ITEM;
				refValue = labResult.getItem().getReferenceMale();
			}
		} else {
			refValue = labResult.getReferenceFemale();
			if (StringUtils.isEmpty(refValue)) {
				description = Description.PATHO_REF_ITEM;
				refValue = labResult.getItem().getReferenceFemale();
			}
		}
		return new Object[] {
			refValue, description
		};
	}
	
	private EvaluationResult evaluateTextualResult(ILabResult labResult,
		final boolean isAbsoluteItem){
		
		String lcResult = labResult.getResult().trim();
		Object[] ref = getReferenceValueForLabResult(labResult);
		String refValue = (String) ref[0];
		Description description =
			(isAbsoluteItem) ? Description.PATHO_ABSOLUT : (Description) ref[1];
		
		if (lcResult.equals(refValue)) {
			return new EvaluationResult(true, false,
				new PathologicDescription(description, lcResult));
		}
		
		if (isAbsoluteItem) {
			if (lcResult.toLowerCase().startsWith("pos")
				|| lcResult.toLowerCase().startsWith("+")) {
				return new EvaluationResult(true, true,
					new PathologicDescription(description, lcResult));
			}
		}
		
		String SELECTED_BASE =
			(isAbsoluteItem) ? Preferences.LABSETTINGS_CFG_EVAL_PREFIX_TYPE_ABSOLUT
					: Preferences.LABSETTINGS_CFG_EVAL_PREFIX_TYPE_TEXT;
		if (ModelUtil.isConfig(
			SELECTED_BASE
				+ Preferences.LABSETTINGS_CFG_EVAL_REFVAL_NON_EQUAL_RESVAL_MEANS_PATHOLOGIC,
			false)) {
			if (!lcResult.equalsIgnoreCase(refValue)) {
				return new EvaluationResult(true, true,
					new PathologicDescription(description, lcResult));
			}
		}
		
		return new EvaluationResult(true, false,
			new PathologicDescription(Description.UNKNOWN, lcResult));
	}
	
	private List<String> parseRefString(String ref){
		List<String> result = new ArrayList<String>();
		if (ref != null && !ref.isEmpty()) {
			Matcher m = refValuesPattern.matcher(ref);
			while (m.find()) {
				result.add(m.group(1).trim());
			}
			// add the whole string if nothing found
			if (result.isEmpty()) {
				result.add(ref.trim());
			}
		}
		return result;
	}
	
	/**
	 * Test result against the provided reference value to determine wheter it is pathologic
	 * 
	 * @param ref
	 * @param result
	 * @return <code>true</code> if pathologic, <code>false</code> if not, <code>null</code> if we
	 *         don't know
	 * @since 3.4 if we can't test a value, as there are no rules, return <code>null</code>
	 */
	private Boolean testRef(String ref, String result){
		try {
			if (ref.trim().startsWith(SMALLER) || ref.trim().startsWith(BIGGER)) {
				String resultSign = null;
				double refVal = Double.parseDouble(ref.substring(1).trim());
				
				if (result.trim().startsWith(SMALLER) || result.trim().startsWith(BIGGER)) {
					resultSign = result.substring(0, 1).trim();
					result = result.substring(1).trim();
				}
				double val = Double.parseDouble(result);
				if (ref.trim().startsWith(SMALLER)) {
					return (val >= refVal && !(val == refVal && SMALLER.equals(resultSign)));
				} else {
					return (val <= refVal && !(val == refVal && BIGGER.equals(resultSign)));
				}
			} else if (ref.contains("-")) {
				String[] range = ref.split("\\s*-\\s*"); //$NON-NLS-1$
				if (range.length == 2) {
					double lower = Double.parseDouble(range[0]);
					double upper = Double.parseDouble(range[1]);
					double val = Double.parseDouble(result);
					return ((val < lower) || (val > upper));
				}
			}
		} catch (NumberFormatException nfe) {
			// don't mind
		}
		// we can't test as we don't have a testing rule
		return null;
	}
}
