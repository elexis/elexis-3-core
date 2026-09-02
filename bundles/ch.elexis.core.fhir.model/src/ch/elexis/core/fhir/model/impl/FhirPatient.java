package ch.elexis.core.fhir.model.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;

import ch.elexis.core.fhir.mapper.r4.util.FhirUtil;
import ch.elexis.core.findings.IdentifierSystem;
import ch.elexis.core.model.IAddress;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IImage;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.IRelatedContact;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.MaritalStatus;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.types.Country;
import ch.elexis.core.types.Gender;

public class FhirPatient extends AbstractFhirModelAdapter<IPatient, Patient> implements IPatient {

	public FhirPatient(Patient fhirResource) {
		super(fhirResource);
	}

	@Override
	public Class<Patient> getFhirType() {
		return Patient.class;
	}

	@Override
	public Class<? extends Identifiable> getModelType() {
		return IPatient.class;
	}

	// ========== GETTERS - All delegate to getLoaded() ==========

	@Override
	public IContact getFamilyDoctor() {
		return getLoaded().getFamilyDoctor();
	}

	@Override
	public String getDiagnosen() {
		return getLoaded().getDiagnosen();
	}

	@Override
	public String getRisk() {
		return getLoaded().getRisk();
	}

	@Override
	public String getFamilyAnamnese() {
		return getLoaded().getFamilyAnamnese();
	}

	@Override
	public String getPersonalAnamnese() {
		return getLoaded().getPersonalAnamnese();
	}

	@Override
	public String getAllergies() {
		return getLoaded().getAllergies();
	}

	@Override
	public List<ICoverage> getCoverages() {
		return getLoaded().getCoverages();
	}

	@Override
	public String getPatientNr() {
		return getLoaded().getPatientNr();
	}

	@Override
	public List<IPrescription> getMedication(List<EntryType> filterType) {
		return getLoaded().getMedication(filterType);
	}

	@Override
	public LocalDateTime getDateOfBirth() {
		return getLoaded().getDateOfBirth();
	}

	@Override
	public Gender getGender() {
		return getLoaded().getGender();
	}

	@Override
	public String getTitel() {
		return getLoaded().getTitel();
	}

	@Override
	public String getTitelSuffix() {
		return getLoaded().getTitelSuffix();
	}

	@Override
	public String getFirstName() {
		return getLoaded().getFirstName();
	}

	@Override
	public String getLastName() {
		return getLoaded().getLastName();
	}

	@Override
	public MaritalStatus getMaritalStatus() {
		return getLoaded().getMaritalStatus();
	}

	@Override
	public IContact getLegalGuardian() {
		return getLoaded().getLegalGuardian();
	}

	@Override
	public LocalDateTime getDateOfDeath() {
		return getLoaded().getDateOfDeath();
	}

	@Override
	public int getAgeInYears() {
		return getLoaded().getAgeInYears();
	}

	@Override
	public long getAgeAtIn(LocalDateTime reference, ChronoUnit chronoUnit) {
		return getLoaded().getAgeAtIn(reference, chronoUnit);
	}

	@Override
	public String getDescription1() {
		return getLoaded().getDescription1();
	}

	@Override
	public String getDescription2() {
		return getLoaded().getDescription2();
	}

	@Override
	public String getDescription3() {
		return getLoaded().getDescription3();
	}

	@Override
	public String getCode() {
		return getLoaded().getCode();
	}

	@Override
	public Country getCountry() {
		return getLoaded().getCountry();
	}

	@Override
	public String getZip() {
		return getLoaded().getZip();
	}

	@Override
	public String getCity() {
		return getLoaded().getCity();
	}

	@Override
	public String getStreet() {
		return getLoaded().getStreet();
	}

	@Override
	public String getPhone1() {
		return getLoaded().getPhone1();
	}

	@Override
	public String getPhone2() {
		return getLoaded().getPhone2();
	}

	@Override
	public String getFax() {
		return getLoaded().getFax();
	}

	@Override
	public String getEmail() {
		return getLoaded().getEmail();
	}

	@Override
	public String getWebsite() {
		return getLoaded().getWebsite();
	}

	@Override
	public String getMobile() {
		return getLoaded().getMobile();
	}

	@Override
	public String getComment() {
		return getLoaded().getComment();
	}

	@Override
	public List<IAddress> getAddress() {
		return getLoaded().getAddress();
	}

	@Override
	public String getGroup() {
		return getLoaded().getGroup();
	}

	@Override
	public String getPostalAddress() {
		return getLoaded().getPostalAddress();
	}

	@Override
	public IImage getImage() {
		return getLoaded().getImage();
	}

	@Override
	public List<IRelatedContact> getRelatedContacts() {
		return getLoaded().getRelatedContacts();
	}

	@Override
	public boolean isDeceased() {
		return getLoaded().isDeceased();
	}

	@Override
	public String getEmail2() {
		return getLoaded().getEmail2();
	}

	@Override
	public IOrganization asIOrganization() {
		return getLoaded().asIOrganization();
	}

	@Override
	public boolean isMandator() {
		return getLoaded().isMandator();
	}

	@Override
	public boolean isUser() {
		return getLoaded().isUser();
	}

	@Override
	public boolean isPatient() {
		return getLoaded().isPatient();
	}

	@Override
	public boolean isPerson() {
		return getLoaded().isPerson();
	}

	@Override
	public boolean isLaboratory() {
		return getLoaded().isLaboratory();
	}

	@Override
	public boolean isOrganization() {
		return getLoaded().isOrganization();
	}

	@Override
	public boolean isDeleted() {
		return getLoaded().isDeleted();
	}

	@Override
	public Object getExtInfo(Object key) {
		return getLoaded().getExtInfo(key);
	}

	@Override
	public Map<Object, Object> getMap() {
		return getLoaded().getMap();
	}

	@Override
	public IPerson asIPerson() {
		return getLoaded().asIPerson();
	}

	@Override
	public IPatient asIPatient() {
		return getLoaded().asIPatient();
	}

	// ========== SETTERS - Keep existing FHIR modification logic ==========

	@Override
	public void setFamilyDoctor(IContact value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setDiagnosen(String value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setRisk(String value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setFamilyAnamnese(String value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setPersonalAnamnese(String value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setAllergies(String value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setPatientNr(String patientNr) {
		Identifier identifier = FhirUtil.getOrCreateIdentifier(IdentifierSystem.ELEXIS_PATNR.getSystem(),
				getFhirResource());
		identifier.setValue(patientNr);
	}

	@Override
	public void setDateOfBirth(LocalDateTime value) {
		if (value != null) {
			getFhirResource().setBirthDate(Date.from(value.atZone(ZoneId.systemDefault()).toInstant()));
		}
		getFhirResource().setBirthDate(null);
	}

	@Override
	public void setGender(Gender value) {
		if (value != null) {
			switch (value) {
			case FEMALE:
				getFhirResource().setGender(AdministrativeGender.FEMALE);
				break;
			case MALE:
				getFhirResource().setGender(AdministrativeGender.MALE);
				break;
			case UNDEFINED:
				getFhirResource().setGender(AdministrativeGender.OTHER);
				break;
			case UNKNOWN:
				getFhirResource().setGender(AdministrativeGender.UNKNOWN);
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void setTitel(String value) {
		if (value != null) {
			getFhirResource().getNameFirstRep().setPrefix(Collections.singletonList(new StringType(value)));
		} else {
			getFhirResource().getNameFirstRep().setPrefix(null);
		}
	}

	@Override
	public void setTitelSuffix(String value) {
		if (value != null) {
			getFhirResource().getNameFirstRep().setSuffix(Collections.singletonList(new StringType(value)));
		} else {
			getFhirResource().getNameFirstRep().setSuffix(null);
		}
	}

	@Override
	public void setFirstName(String value) {
		if (value != null) {
			getFhirResource().getNameFirstRep().setGiven(Collections.singletonList(new StringType(value)));
		} else {
			getFhirResource().getNameFirstRep().setGiven(null);
		}
	}

	@Override
	public void setLastName(String value) {
		getFhirResource().getNameFirstRep().setFamily(value);
	}

	@Override
	public void setMaritalStatus(MaritalStatus value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setLegalGuardian(IContact value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setDateOfDeath(LocalDateTime value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setDescription1(String value) {
		setLastName(value);
	}

	@Override
	public void setDescription2(String value) {
		setFirstName(value);
	}

	@Override
	public void setDescription3(String value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setCode(String value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setCountry(Country value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setZip(String value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setCity(String value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setStreet(String value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setPhone1(String value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setPhone2(String value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setFax(String value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setEmail(String value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setWebsite(String value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setMobile(String value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setComment(String value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setGroup(String value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setPostalAddress(String value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setImage(IImage value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setDeceased(boolean value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setEmail2(String value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setMandator(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setUser(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPatient(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPerson(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLaboratory(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setOrganization(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDeleted(boolean value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setExtInfo(Object key, Object value) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean addXid(String domain, String id, boolean updateIfExists) {
		// TODO Auto-generated method stub
		return false;
	}
}
