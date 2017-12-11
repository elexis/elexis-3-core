package ch.elexis.core.findings.fhir.po.dataaccess;

import java.util.List;
import java.util.Optional;

import ch.elexis.core.findings.IAllergyIntolerance;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.ICondition.ConditionStatus;
import ch.elexis.core.findings.IFamilyMemberHistory;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservation.ObservationCategory;
import ch.elexis.core.findings.IObservation.ObservationCode;
import ch.elexis.core.findings.codes.ICodingService;

public class TextUtil {
	
	/**
	 * Test if an {@link IObservation} represents a risk factor.
	 * 
	 * @param iFinding
	 * @return
	 */
	public static boolean isRiskfactor(IObservation iFinding){
		if (iFinding.getCategory() == ObservationCategory.SOCIALHISTORY) {
			for (ICoding code : iFinding.getCoding()) {
				if (ObservationCode.ANAM_RISK.isSame(code)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Test if an {@link IObservation} represents a personal anamnesis.
	 * 
	 * @param iFinding
	 * @return
	 */
	public static boolean isPersAnamnese(IObservation iFinding){
		if (iFinding instanceof IObservation
			&& ((IObservation) iFinding).getCategory() == ObservationCategory.SOCIALHISTORY) {
			for (ICoding code : iFinding.getCoding()) {
				if (ObservationCode.ANAM_PERSONAL.isSame(code)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Get text representation of an {@link ICondition}.
	 * 
	 * @param condition
	 * @param codingService
	 * @return
	 */
	public static String getText(ICondition condition, ICodingService codingService){
		StringBuilder sb = new StringBuilder();
		ConditionStatus status = condition.getStatus();
		sb.append(status.getLocalized()).append(" (");
		Optional<String> start = condition.getStart();
		sb.append(start.orElse("")).append(" - ");
		Optional<String> end = condition.getEnd();
		sb.append(end.orElse(""));
		sb.append(") ");
		
		Optional<String> text = condition.getText();
		boolean multiline = text.isPresent() && text.get().contains("\n");
		sb.append(text.orElse("")).append(multiline ? "\n" : "");
		
		List<ICoding> coding = condition.getCoding();
		for (ICoding iCoding : coding) {
			sb.append(" [").append(codingService.getShortLabel(iCoding)).append("] ");
		}
		
		return sb.toString();
	}
	
	/**
	 * Get text representation of an {@link IObservation}.
	 * 
	 * @param observation
	 * @param codingService
	 * @return
	 */
	public static String getText(IObservation observation, ICodingService codingService){
		StringBuilder sb = new StringBuilder();
		if (isPersAnamnese(observation)) {
			sb.append(observation.getText().orElse(""));
		} else if (isRiskfactor(observation)) {
			sb.append(observation.getText().orElse(""));
		}
		return sb.toString();
	}
	

	/**
	 * Get text representation of an {@link IAllergyIntolerance}.
	 * 
	 * @param allergy
	 * @param codingService
	 * @return
	 */
	public static Object getText(IAllergyIntolerance allergy, ICodingService codingService){
		StringBuilder sb = new StringBuilder();
		sb.append(allergy.getText().orElse(""));
		return sb.toString();
	}
	
	/**
	 * Get text representation of an {@link IFamilyMemberHistory}.
	 * 
	 * @param famanam
	 * @param codingService
	 * @return
	 */
	public static Object getText(IFamilyMemberHistory famanam, ICodingService codingService){
		StringBuilder sb = new StringBuilder();
		sb.append(famanam.getText().orElse(""));
		return sb.toString();
	}
}
