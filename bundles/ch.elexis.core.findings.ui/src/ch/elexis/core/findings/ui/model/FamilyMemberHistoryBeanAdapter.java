package ch.elexis.core.findings.ui.model;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.findings.IFamilyMemberHistory;

public class FamilyMemberHistoryBeanAdapter extends AbstractBeanAdapter<IFamilyMemberHistory> {
	
	public FamilyMemberHistoryBeanAdapter(IFamilyMemberHistory familyMemberHistory){
		this.finding = familyMemberHistory;
		if (StringUtils.isBlank(familyMemberHistory.getPatientId())) {
			init();
		}
	}
}
