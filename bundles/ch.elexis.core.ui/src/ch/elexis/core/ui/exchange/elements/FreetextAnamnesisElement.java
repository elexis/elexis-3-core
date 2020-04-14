package ch.elexis.core.ui.exchange.elements;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.ui.exchange.XChangeExporter;
import ch.elexis.data.Patient;

public class FreetextAnamnesisElement extends XChangeElement {
	
	public static final String XML_NAME = "freetextAnamnesis";
	
	public static final String ATTR_PERSONAL = "personal";
	public static final String ATTR_FAMILY = "family";
	
	@Override
	public String getXMLName(){
		return XML_NAME;
	}

	public FreetextAnamnesisElement asExporter(XChangeExporter p, Patient patient){
		asExporter(p);
		String personalAnamnese = patient.getPersonalAnamnese();
		if(StringUtils.isNotBlank(personalAnamnese)) {
			setAttribute(ATTR_PERSONAL, personalAnamnese);
		}
		String familyAnamnese = patient.getFamilyAnamnese();
		if(StringUtils.isNotBlank(familyAnamnese)) {
			setAttribute(ATTR_FAMILY, familyAnamnese);
		}
		return this;
	}
	
}
