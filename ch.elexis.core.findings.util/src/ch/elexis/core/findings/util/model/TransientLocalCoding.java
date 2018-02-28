package ch.elexis.core.findings.util.model;

import java.util.List;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ILocalCoding;
import ch.elexis.core.findings.IObservation.ObservationCode;

public class TransientLocalCoding implements ILocalCoding {
	
	private String system;
	private String code;
	private String display;
	private List<ICoding> mappedCodes;
	
	public TransientLocalCoding(String system, String code, String display){
		this.system = system;
		this.code = code;
		this.display = display;
	}
	
	public TransientLocalCoding(ObservationCode code){
		this.system = code.getIdentifierSystem().getSystem();
		this.code = code.getCode();
		this.display = "";
	}
	
	@Override
	public String getSystem(){
		return system;
	}
	
	@Override
	public String getCode(){
		return code;
	}
	
	@Override
	public String getDisplay(){
		return display;
	}

	@Override
	public List<ICoding> getMappedCodes() {
		return mappedCodes;
	}

	@Override
	public void setMappedCodes(List<ICoding> mappedCodes) {
		this.mappedCodes = mappedCodes;
	}
	
}
