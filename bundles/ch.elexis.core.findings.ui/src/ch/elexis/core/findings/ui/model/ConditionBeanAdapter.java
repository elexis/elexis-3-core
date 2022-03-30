package ch.elexis.core.findings.ui.model;

import java.time.LocalDate;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.ICondition.ConditionCategory;
import ch.elexis.core.findings.ICondition.ConditionStatus;

public class ConditionBeanAdapter extends AbstractBeanAdapter<ICondition> {

	public ConditionBeanAdapter(ICondition condition) {
		this.finding = condition;
		if (StringUtils.isBlank(condition.getPatientId())) {
			init();
		}
	}

	public ConditionCategory getCategory() {
		return finding.getCategory();
	}

	public void setCategory(ConditionCategory category) {
		finding.setCategory(category);
		autoSave();
	}

	public ConditionStatus getStatus() {
		return finding.getStatus();
	}

	public void setStatus(ConditionStatus status) {
		finding.setStatus(status);
		autoSave();
	}

	public List<ICoding> getCoding() {
		return finding.getCoding();
	}

	public void setCoding(List<ICoding> coding) {
		finding.setCoding(coding);
		autoSave();
	}

	public void setDateRecorded(LocalDate date) {
		finding.setDateRecorded(date);
		autoSave();
	}

	public LocalDate getDateRecorded() {
		return finding.getDateRecorded().orElse(null);
	}

	public void setStart(String start) {
		finding.setStart(start);
		autoSave();
	}

	public String getStart() {
		return finding.getStart().orElse("");
	}

	public void setEnd(String end) {
		finding.setEnd(end);
		autoSave();
	}

	public String getEnd() {
		return finding.getEnd().orElse("");
	}

	public void addNote(String text) {
		finding.addNote(text);
		autoSave();
	}

	public void removeNote(String text) {
		finding.removeNote(text);
		autoSave();
	}

	public List<String> getNotes() {
		return finding.getNotes();
	}
}
