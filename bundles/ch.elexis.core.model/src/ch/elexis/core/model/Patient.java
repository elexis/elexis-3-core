package ch.elexis.core.model;

import java.util.List;
import java.util.stream.Collectors;

import ch.elexis.core.jpa.entities.Fall;
import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.model.util.ModelUtil;

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
	
	@Override
	public List<ICoverage> getCoverages(){
		return getEntity().getFaelle().parallelStream().filter(f -> !f.isDeleted())
			.map(f -> ModelUtil.getAdapter(f, ICoverage.class)).collect(Collectors.toList());
	}


	@Override
	public ICoverage addCoverage(ICoverage coverage){
		@SuppressWarnings("unchecked")
		Fall fall = ((AbstractIdModelAdapter<Fall>) coverage).getEntity();
		getEntity().getFaelle().add(fall);
		return coverage;
	}

}
