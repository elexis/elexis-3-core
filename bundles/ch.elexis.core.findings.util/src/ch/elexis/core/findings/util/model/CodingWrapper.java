package ch.elexis.core.findings.util.model;

import org.hl7.fhir.dstu3.model.Coding;

import ch.elexis.core.findings.ICoding;

public class CodingWrapper implements ICoding {
	private Coding coding;
	
	public CodingWrapper(Coding coding){
		this.coding = coding;
	}
	
	@Override
	public String getSystem(){
		return coding.getSystem();
	}
	
	@Override
	public String getCode(){
		return coding.getCode();
	}
	
	@Override
	public String getDisplay(){
		return coding.getDisplay();
	}
}
