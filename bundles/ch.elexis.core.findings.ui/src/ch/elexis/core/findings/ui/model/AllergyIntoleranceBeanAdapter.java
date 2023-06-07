package ch.elexis.core.findings.ui.model;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.findings.IAllergyIntolerance;
import ch.elexis.core.findings.IAllergyIntolerance.AllergyIntoleranceCategory;

public class AllergyIntoleranceBeanAdapter extends AbstractBeanAdapter<IAllergyIntolerance> {

	public AllergyIntoleranceBeanAdapter(IAllergyIntolerance allergyIntolerance) {
		this.finding = allergyIntolerance;
		if (StringUtils.isBlank(allergyIntolerance.getPatientId())) {
			init();
		}
	}

	public AllergyIntoleranceCategory getCategory() {
		return finding.getCategory();
	}

	public void setCategory(AllergyIntoleranceCategory category) {
		finding.setCategory(category);
	}
}
