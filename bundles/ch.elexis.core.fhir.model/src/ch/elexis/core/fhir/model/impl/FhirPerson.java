package ch.elexis.core.fhir.model.impl;

import java.time.LocalDateTime;

import org.hl7.fhir.r4.model.Person;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.MaritalStatus;
import ch.elexis.core.types.Gender;

public class FhirPerson extends AbstractFhirContactModelAdapter<IPerson, Person> implements IPerson {

	public FhirPerson(Person fhirResource) {
		super(fhirResource);
	}

	@Override
	public Class<Person> getFhirType() {
		return Person.class;
	}

	@Override
	public Class<? extends Identifiable> getModelType() {
		return IPerson.class;
	}

	@Override
	public boolean isPatient() {
		return false;
	}

	@Override
	public boolean isDeceased() {
		return getLoaded().isDeceased();
	}

	@Override
	public void setDeceased(boolean value) {
		getLoadedMarkDirty().setDeceased(value);
	}

	@Override
	public LocalDateTime getDateOfBirth() {
		return getLoaded().getDateOfBirth();
	}

	@Override
	public void setDateOfBirth(LocalDateTime value) {
		getLoadedMarkDirty().setDateOfBirth(value);
	}

	@Override
	public Gender getGender() {
		return getLoaded().getGender();
	}

	@Override
	public void setGender(Gender value) {
		getLoadedMarkDirty().setGender(value);
	}

	@Override
	public String getTitel() {
		return getLoaded().getTitel();
	}

	@Override
	public void setTitel(String value) {
		getLoadedMarkDirty().setTitel(value);
	}

	@Override
	public String getTitelSuffix() {
		return getLoaded().getTitelSuffix();
	}

	@Override
	public void setTitelSuffix(String value) {
		getLoadedMarkDirty().setTitelSuffix(value);
	}

	@Override
	public String getFirstName() {
		return getLoaded().getFirstName();
	}

	@Override
	public void setFirstName(String value) {
		getLoadedMarkDirty().setFirstName(value);

	}

	@Override
	public String getLastName() {
		return getLoaded().getLastName();
	}

	@Override
	public void setLastName(String value) {
		getLoadedMarkDirty().setLastName(value);
	}

	@Override
	public MaritalStatus getMaritalStatus() {
		return getLoaded().getMaritalStatus();
	}

	@Override
	public void setMaritalStatus(MaritalStatus value) {
		getLoadedMarkDirty().setMaritalStatus(value);
	}

	@Override
	public IContact getLegalGuardian() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLegalGuardian(IContact value) {
		getLoadedMarkDirty().setLegalGuardian(value);
	}

	@Override
	public LocalDateTime getDateOfDeath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDateOfDeath(LocalDateTime value) {
		getLoadedMarkDirty().setDateOfDeath(value);
	}

}
