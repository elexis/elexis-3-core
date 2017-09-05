package ch.elexis.data.dto;

import ch.elexis.core.data.interfaces.IDiagnose;

public class DiagnosesDTO {
	private final String id;
	private String label;
	
	public DiagnosesDTO(IDiagnose iDiagnose){
		this.id = iDiagnose.getId();
		this.label = iDiagnose.getLabel();
	}
	
	public String getId(){
		return id;
	}
	
	public void setLabel(String label){
		this.label = label;
	}
	
	public String getLabel(){
		return label;
	}
}