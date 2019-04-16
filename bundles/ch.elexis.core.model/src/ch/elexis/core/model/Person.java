package ch.elexis.core.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.model.format.PersonFormatUtil;
import ch.elexis.core.model.service.holder.CoreModelServiceHolder;
import ch.elexis.core.types.Gender;

public class Person extends Contact implements IPerson {
	
	public Person(Kontakt model){
		super(model);
	}
	
	@Override
	public Gender getGender(){
		return getEntity().getGender();
	}
	
	@Override
	public void setGender(Gender value){
		getEntity().setGender(value);
	}
	
	@Override
	public String getTitel(){
		return getEntity().getTitel();
	}
	
	@Override
	public void setTitel(String value){
		getEntity().setTitel(value);
	}
	
	@Override
	public String getTitelSuffix(){
		return getEntity().getTitelSuffix();
	}
	
	@Override
	public void setTitelSuffix(String value){
		getEntity().setTitelSuffix(value);
	}
	
	@Override
	public String getFirstName(){
		return getEntity().getDescription2();
	}
	
	@Override
	public LocalDateTime getDateOfBirth(){
		if (getEntity().getDob() != null) {
			return getEntity().getDob().atStartOfDay();
		}
		return null;
	}
	
	@Override
	public void setDateOfBirth(LocalDateTime value){
		getEntity().setDob(value.toLocalDate());
	}
	
	@Override
	public void setFirstName(String value){
		getEntity().setDescription2(value);
	}
	
	@Override
	public String getLastName(){
		return getEntity().getDescription1();
	}
	
	@Override
	public void setLastName(String value){
		getEntity().setDescription1(value);
	}
	
	@Override
	public int getAgeInYears(){
		LocalDateTime dateOfBirth = getDateOfBirth();
		if (dateOfBirth != null) {
			LocalDate now = LocalDate.now();
			long years = ChronoUnit.YEARS.between(dateOfBirth.toLocalDate(), now);
			return (int) years;
		}
		return -1;
	}
	
	@Override
	public long getAgeAtIn(LocalDateTime reference, ChronoUnit chronoUnit){
		return chronoUnit.between(getDateOfBirth(), reference);
	}
	
	@Override
	public MaritalStatus getMaritalStatus(){
		Object extInfo = getExtInfo(PatientConstants.FLD_EXTINFO_MARITAL_STATUS);
		if (extInfo != null) {
			return MaritalStatus.byNumericSafe((String) extInfo);
		}
		return null;
	}
	
	@Override
	public void setMaritalStatus(MaritalStatus maritalStatus){
		setExtInfo(PatientConstants.FLD_EXTINFO_MARITAL_STATUS,
			(maritalStatus != null) ? Integer.toString(maritalStatus.numericValue()) : null);
	}
	
	@Override
	public IPerson getLegalGuardian(){
		Object legalGuardianId = getExtInfo(PatientConstants.FLD_EXTINFO_LEGAL_GUARDIAN);
		if (legalGuardianId instanceof String) {
			Optional<IPerson> owner =
				CoreModelServiceHolder.get().load((String) legalGuardianId, IPerson.class);
			return owner.orElse(null);
		}
		return null;
	}
	
	@Override
	public void setLegalGuardian(IPerson value){
		if (value instanceof AbstractIdDeleteModelAdapter) {
			setExtInfo(PatientConstants.FLD_EXTINFO_LEGAL_GUARDIAN, value.getId());
		} else if (value == null) {
			setExtInfo(PatientConstants.FLD_EXTINFO_LEGAL_GUARDIAN, null);
		}
	}
	
	@Override
	public String getLabel(){
		return PersonFormatUtil.getPersonalia(this);
	}
}
