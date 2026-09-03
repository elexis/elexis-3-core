package ch.elexis.core.findings.fhir.po.dataaccess;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

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
	 *
	 * @param iFinding
	 * @return
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
	 *
	 * @param iFinding
	 * @return
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
	 * @param wordFormat
	 * @return
	 */
	public static String getText(ICondition condition, ICodingService codingService, boolean wordFormat) {
		StringBuilder sb = new StringBuilder();
		Optional<String> start = condition.getStart();
		Optional<String> end = condition.getEnd();

		if (wordFormat) {
			start.ifPresent(s -> sb.append("<p><strong>").append(escapeHtml(s)).append("</strong></p>"));
			condition.getText().filter(StringUtils::isNotBlank).ifPresent(t -> sb.append(toBlockHtml(t)));
			for (String note : condition.getNotes()) {
				if (StringUtils.isNotBlank(note)) {
					sb.append(toBlockHtml(note));
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

	/** The tags the rich text editor writes, tells its markup from plain text of earlier versions. */
	private static final Pattern HTML_TAG = Pattern
			.compile("(?i)</?(?:p|br|div|ul|ol|li|strong|b|em|i|u|s|strike|del|span|font|h[1-6]|sub|sup)"
					+ "(?:\\s[^>]*)?\\s*/?>");

	private static String toBlockHtml(String text) {
		if (StringUtils.isBlank(text)) {
			return StringUtils.EMPTY;
		}
		if (HTML_TAG.matcher(text).find()) {
			return text;
		}
		StringBuilder html = new StringBuilder();
		boolean inList = false;
		for (String line : text.split("\\r?\\n")) {
			String content = line.trim();
			boolean item = content.startsWith("-");
			if (item) {
				content = content.substring(1).trim();
			}
			if (content.isEmpty()) {
				continue;
			}
			if (item != inList) {
				html.append(item ? "<ul>" : "</ul>");
				inList = item;
			}
			html.append(item ? "<li>" : "<p>").append(escapeHtml(content)).append(item ? "</li>" : "</p>");
		}
		if (inList) {
			html.append("</ul>");
		}
		return html.toString();
	}

	private static String escapeHtml(String text) {
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	/**
	 * Text representation of an {@link IObservation} (personal anamnesis or risk
	 * factor).
	 *
	 * @param observation
	 * @param codingService
	 * @return
	 */
	public static String getText(IObservation observation, ICodingService codingService) {
		if (isPersAnamnese(observation) || isRiskfactor(observation)) {
			return observation.getText().orElse(StringUtils.EMPTY);
		}
		return StringUtils.EMPTY;
	}

	/**
	 * Text representation of an {@link IAllergyIntolerance}.
	 *
	 * @param allergy
	 * @param codingService
	 * @return
	 */
	public static String getText(IAllergyIntolerance allergy, ICodingService codingService) {
		return allergy.getText().orElse(StringUtils.EMPTY);
	}

	/**
	 * Text representation of an {@link IFamilyMemberHistory}.
	 *
	 * @param famanam
	 * @param codingService
	 * @return
	 */
	public static String getText(IFamilyMemberHistory famanam, ICodingService codingService) {
		return famanam.getText().orElse(StringUtils.EMPTY);
	}
}
