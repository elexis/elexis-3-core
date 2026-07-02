package ch.elexis.core.findings.fhir.po.dataaccess;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.findings.IAllergyIntolerance;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.IFamilyMemberHistory;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservation.ObservationCategory;
import ch.elexis.core.findings.IObservation.ObservationCode;
import ch.elexis.core.findings.codes.ICodingService;

public class TextUtil {

	/**
	 * Test if an {@link IObservation} represents a risk factor.
	 */
	public static boolean isRiskfactor(IObservation iFinding) {
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
	 */
	public static boolean isPersAnamnese(IObservation iFinding) {
		if (iFinding.getCategory() == ObservationCategory.SOCIALHISTORY) {
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
	public static String getText(ICondition condition, ICodingService codingService, boolean wordFormat) {
		StringBuilder sb = new StringBuilder();
		Optional<String> start = condition.getStart();
		Optional<String> end = condition.getEnd();

		if (wordFormat) {
			start.ifPresent(s -> sb.append("<strong>").append(s).append("</strong>").append(StringUtils.LF)
					.append(StringUtils.LF));
			condition.getText().filter(StringUtils::isNotBlank).ifPresent(t -> sb.append(t).append(StringUtils.LF));
			for (String note : condition.getNotes()) {
				if (StringUtils.isNotBlank(note)) {
					sb.append(note).append(StringUtils.LF);
				}
			}
		} else {
			if (start.isPresent() || end.isPresent()) {
				sb.append("(");
				sb.append(start.orElse(StringUtils.EMPTY)).append(" - ");
				sb.append(end.orElse(StringUtils.EMPTY));
				sb.append(") ");
			}
			Optional<String> text = condition.getText();
			boolean multiline = text.isPresent() && text.get().contains(StringUtils.LF);
			sb.append(text.orElse(StringUtils.EMPTY)).append(multiline ? StringUtils.LF : StringUtils.EMPTY);
			List<ICoding> coding = condition.getCoding();
			for (ICoding iCoding : coding) {
				sb.append(" [").append(codingService.getShortLabel(iCoding)).append("] ");
			}
		}

		return sb.toString();
	}

	/**
	 * Text representation of an {@link IObservation} (personal anamnesis or risk
	 * factor).
	 */
	public static String getText(IObservation observation, ICodingService codingService) {
		if (isPersAnamnese(observation) || isRiskfactor(observation)) {
			return observation.getText().orElse(StringUtils.EMPTY);
		}
		return StringUtils.EMPTY;
	}

	/**
	 * Text representation of an {@link IAllergyIntolerance}.
	 */
	public static String getText(IAllergyIntolerance allergy, ICodingService codingService) {
		return allergy.getText().orElse(StringUtils.EMPTY);
	}

	/**
	 * Text representation of an {@link IFamilyMemberHistory}.
	 */
	public static String getText(IFamilyMemberHistory famanam, ICodingService codingService) {
		return famanam.getText().orElse(StringUtils.EMPTY);
	}
}
