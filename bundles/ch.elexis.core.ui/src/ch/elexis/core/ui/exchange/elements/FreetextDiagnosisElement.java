package ch.elexis.core.ui.exchange.elements;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.ui.exchange.XChangeExporter;
import ch.elexis.data.Patient;

public class FreetextDiagnosisElement extends XChangeElement {
	
	public static final String XML_NAME = "freetextDiagnosis";
	
	@Override
	public String getXMLName(){
		return XML_NAME;
	}

	public FreetextDiagnosisElement asExporter(XChangeExporter p, Patient patient){
		asExporter(p);
		String diagnosis = patient.getDiagnosen();
		if(StringUtils.isNotBlank(diagnosis)) {
			setAttribute(ATTR_VALUE, diagnosis);
		}
		return this;
	}
	
}
