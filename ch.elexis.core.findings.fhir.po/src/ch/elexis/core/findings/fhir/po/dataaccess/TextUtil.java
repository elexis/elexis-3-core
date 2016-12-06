package ch.elexis.core.findings.fhir.po.dataaccess;

import java.util.List;
import java.util.Optional;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.ICondition.ConditionStatus;
import ch.elexis.core.findings.codes.ICodingService;

public class TextUtil {
	
	/**
	 * Get text representation of a ICondition.
	 * 
	 * @param condition
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
}
