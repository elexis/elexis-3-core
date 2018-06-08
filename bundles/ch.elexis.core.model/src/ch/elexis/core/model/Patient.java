package ch.elexis.core.model;

import ch.elexis.core.jpa.entities.Kontakt;

public class Patient extends Person implements IPatient {
	
	public Patient(Kontakt model){
		super(model);
	}
	
	@Override
	public String getDiagnosen(){
		return getEntity().getDiagnosen();
	}
	
	@Override
	public void setDiagnosen(String value){
		getEntity().setDiagnosen(value);
	}
	
	@Override
	public String getRisk(){
		return getEntity().getRisk();
	}
	
	@Override
	public void setRisk(String value){
		getEntity().setRisk(value);
	}
	
	@Override
	public String getFamilyAnamnese(){
		return getEntity().getFamilyAnamnese();
	}
	
	@Override
	public void setFamilyAnamnese(String value){
		getEntity().setFamilyAnamnese(value);
	}
	
	@Override
	public String getPersonalAnamnese(){
		return getEntity().getPersonalAnamnese();
	}
	
	@Override
	public void setPersonalAnamnese(String value){
		getEntity().setPersonalAnamnese(value);
	}
	
	@Override
	public String getAllergies(){
		return getEntity().getAllergies();
	}
	
	@Override
	public void setAllergies(String value){
		getEntity().setAllergies(value);
	}
	
	@Override
	public String getPatientNr(){
		return getCode();
	}
	
	@Override
	public void setPatientNr(String patientNr){
		setCode(patientNr);
	}
	
	@Override
	public String getPatientLabel(){
		return getLabel();
	}
}
