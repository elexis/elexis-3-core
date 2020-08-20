package ch.elexis.data.dto;

import ch.elexis.core.model.IDiagnosis;

public class DiagnosesDTO {
	private final String id;
	private String label;
	private final IDiagnosis iDiagnose;
	
	public DiagnosesDTO(IDiagnosis iDiagnosis){
		this.id = iDiagnosis.getId();
		this.label = iDiagnosis.getLabel();
		this.iDiagnose = iDiagnosis;
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
	
	public IDiagnosis getiDiagnose(){
		return iDiagnose;
	}
}