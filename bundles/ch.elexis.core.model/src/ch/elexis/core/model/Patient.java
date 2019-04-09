package ch.elexis.core.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.stream.Collectors;

import ch.elexis.core.jpa.entities.Fall;
import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.model.util.internal.ModelUtil;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

public class Patient extends Person implements IPatient {
	
	private DateTimeFormatter localizedBirthDateFormatter;
	
	public Patient(Kontakt model){
		super(model);
		localizedBirthDateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
	}
	
	
	@Override
	public String getLabel() {
		return ((getTitel() != null) ? getTitel() + " " : "") + getFirstName() + " " + getLastName() + " ("
				+ getGenderCharLocalized() + "), " + getDateOfBirth().format(localizedBirthDateFormatter) + " ("
				+ getAgeInYears() + ") - ["+getPatientNr()+"]";
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
	public List<ICoverage> getCoverages(){
		return getEntity().getFaelle().stream().filter(f -> !f.isDeleted())
			.map(f -> ModelUtil.getAdapter(f, ICoverage.class)).collect(Collectors.toList());
	}


	@Override
	public ICoverage addCoverage(ICoverage coverage){
		@SuppressWarnings("unchecked")
		Fall fall = ((AbstractIdModelAdapter<Fall>) coverage).getEntity();
		getEntity().getFaelle().add(fall);
		return coverage;
	}

	@Override
	public List<IPrescription> getMedication(List<EntryType> filterType){
		IQuery<IPrescription> query = CoreModelServiceHolder.get().getQuery(IPrescription.class);
		query.and(ModelPackage.Literals.IPRESCRIPTION__PATIENT, COMPARATOR.EQUALS, this);
		query.startGroup();
		query.or(ModelPackage.Literals.IPRESCRIPTION__DATE_TO, COMPARATOR.EQUALS, null);
		query.or(ModelPackage.Literals.IPRESCRIPTION__DATE_TO, COMPARATOR.GREATER,
			LocalDateTime.now());
		query.andJoinGroups();
		if (filterType != null && !filterType.isEmpty()) {
			query.startGroup();
			for (EntryType entryType : filterType) {
				query.or(ModelPackage.Literals.IPRESCRIPTION__ENTRY_TYPE, COMPARATOR.EQUALS,
					entryType);				
			}
			query.andJoinGroups();
		}
		return query.execute();
	}
	
	@Override
	public IContact getFamilyDoctor(){
		String doctorId = (String) getExtInfo(PatientConstants.FLD_EXTINFO_STAMMARZT);
		if (doctorId != null) {
			return ch.elexis.core.model.service.holder.CoreModelServiceHolder.get().load(doctorId,
				IContact.class).orElse(null);
		}
		return null;
	}
	
	@Override
	public void setFamilyDoctor(IContact value){
		if (value != null) {
			setExtInfo(PatientConstants.FLD_EXTINFO_STAMMARZT, value.getId());
		} else {
			setExtInfo(PatientConstants.FLD_EXTINFO_STAMMARZT, null);
		}
	}
	
}
