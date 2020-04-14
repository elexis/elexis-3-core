package ch.elexis.core.ui.exchange.elements;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.ui.exchange.XChangeExporter;
import ch.elexis.data.Patient;

public class FreetextAnamnesisElement extends XChangeElement {
	
	@Override
	public String getXMLName(){
		return "freetextAnamnesis";
	}

	public FreetextAnamnesisElement asExporter(XChangeExporter p, Patient patient){
		asExporter(p);
		String personalAnamnese = patient.getPersonalAnamnese();
		if(StringUtils.isNotBlank(personalAnamnese)) {
			setAttribute("personal", personalAnamnese);
		}
		String familyAnamnese = patient.getFamilyAnamnese();
		if(StringUtils.isNotBlank(familyAnamnese)) {
			setAttribute("family", familyAnamnese);
		}
		return this;
	}
	
}
