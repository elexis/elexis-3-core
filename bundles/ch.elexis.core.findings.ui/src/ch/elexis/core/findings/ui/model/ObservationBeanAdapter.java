package ch.elexis.core.findings.ui.model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservation.ObservationCategory;

public class ObservationBeanAdapter extends AbstractBeanAdapter<IObservation> {

	public ObservationBeanAdapter(IObservation observation) {
		this.finding = observation;
		if (StringUtils.isBlank(observation.getPatientId())) {
			init();
		}
	}

	public ObservationBeanAdapter category(ObservationCategory category) {
		finding.setCategory(category);
		return this;
	}

	public ObservationBeanAdapter coding(ICoding coding) {
		finding.setCoding(Collections.singletonList(coding));
		return this;
	}

	public ObservationCategory getCategory() {
		return finding.getCategory();
	}

	public void setCategory(ObservationCategory category) {
		finding.setCategory(category);
		autoSave();
	}

	public String getStringValue() {
		return finding.getStringValue().orElse(StringUtils.EMPTY);
	}

	public void setStringValue(String value) {
		finding.setStringValue(value);
		autoSave();
	}

	public List<ICoding> getCoding() {
		return finding.getCoding();
	}

	public void setCoding(List<ICoding> coding) {
		finding.setCoding(coding);
		autoSave();
	}

	public void setEffectiveTime(LocalDateTime date) {
		finding.setEffectiveTime(date);
		autoSave();
	}

	public LocalDateTime getEffectiveTime() {
		return finding.getEffectiveTime().orElse(null);
	}
}
