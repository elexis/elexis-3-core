package ch.elexis.core.fhir.model.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;

import ch.elexis.core.findings.IdentifierSystem;
import ch.elexis.core.findings.util.fhir.transformer.helper.FhirUtil;
import ch.elexis.core.model.IAddress;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IImage;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.IRelatedContact;
import ch.elexis.core.model.MaritalStatus;
import ch.elexis.core.model.format.PersonFormatUtil;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.types.Country;
import ch.elexis.core.types.Gender;

public class FhirPatient extends AbstractFhirModelAdapter<Patient> implements IPatient {

	public FhirPatient(Patient fhirResource) {
		super(fhirResource);
	}

	@Override
	public IContact getFamilyDoctor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFamilyDoctor(IContact value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getDiagnosen() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDiagnosen(String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getRisk() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRisk(String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getFamilyAnamnese() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFamilyAnamnese(String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getPersonalAnamnese() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPersonalAnamnese(String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getAllergies() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAllergies(String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<ICoverage> getCoverages() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPatientNr() {
		return getFhirResource().getIdentifier().stream()
				.filter(i -> IdentifierSystem.ELEXIS_PATNR.getSystem().equals(i.getSystem())).map(i -> i.getValue())
				.findAny().orElse(null);
	}

	@Override
	public void setPatientNr(String patientNr) {
		Identifier identifier = FhirUtil.getOrCreateIdentifier(IdentifierSystem.ELEXIS_PATNR.getSystem(),
				getFhirResource());
		identifier.setValue(patientNr);
	}

	@Override
	public List<IPrescription> getMedication(List<EntryType> filterType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LocalDateTime getDateOfBirth() {
		if (getFhirResource().getBirthDate() != null) {
			return LocalDateTime.ofInstant(getFhirResource().getBirthDate().toInstant(), ZoneId.systemDefault());
		}
		return null;
	}

	@Override
	public void setDateOfBirth(LocalDateTime value) {
		if (value != null) {
			getFhirResource().setBirthDate(Date.from(value.atZone(ZoneId.systemDefault()).toInstant()));
		}
		getFhirResource().setBirthDate(null);
	}

	@Override
	public Gender getGender() {
		if (getFhirResource().getGender() != null) {
			switch (getFhirResource().getGender()) {
			case FEMALE:
				return Gender.FEMALE;
			case MALE:
				return Gender.MALE;
			case UNKNOWN:
				return Gender.UNKNOWN;
			default:
				return Gender.UNDEFINED;
			}
		}
		return Gender.UNDEFINED;
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
	public String getTitel() {
		for (HumanName humanName : getFhirResource().getName()) {
			if (getFhirResource().getName().size() == 1 || HumanName.NameUse.OFFICIAL.equals(humanName.getUse())) {
				return humanName.getPrefixAsSingleString();
			}
		}
		return null;
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
	public String getTitelSuffix() {
		for (HumanName humanName : getFhirResource().getName()) {
			if (getFhirResource().getName().size() == 1 || HumanName.NameUse.OFFICIAL.equals(humanName.getUse())) {
				return humanName.getSuffixAsSingleString();
			}
		}
		return null;
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
	public String getFirstName() {
		for (HumanName humanName : getFhirResource().getName()) {
			if (getFhirResource().getName().size() == 1 || HumanName.NameUse.OFFICIAL.equals(humanName.getUse())) {
				return humanName.getGivenAsSingleString();
			}
		}
		return null;
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
	public String getLastName() {
		for (HumanName humanName : getFhirResource().getName()) {
			if (getFhirResource().getName().size() == 1 || HumanName.NameUse.OFFICIAL.equals(humanName.getUse())) {
				return humanName.getFamily();
			}
		}
		return null;

	}

	@Override
	public void setLastName(String value) {
		getFhirResource().getNameFirstRep().setFamily(value);
	}

	@Override
	public MaritalStatus getMaritalStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMaritalStatus(MaritalStatus value) {
		// TODO Auto-generated method stub

	}

	@Override
	public IContact getLegalGuardian() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLegalGuardian(IContact value) {
		// TODO Auto-generated method stub

	}

	@Override
	public LocalDateTime getDateOfDeath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDateOfDeath(LocalDateTime value) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getAgeInYears() {
		LocalDateTime dateOfBirth = getDateOfBirth();
		if (dateOfBirth != null) {
			LocalDate now = LocalDate.now();
			long years = ChronoUnit.YEARS.between(dateOfBirth.toLocalDate(), now);
			return (int) years;
		}
		return -1;
	}

	@Override
	public long getAgeAtIn(LocalDateTime reference, ChronoUnit chronoUnit) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isMandator() {
		return false;
	}

	@Override
	public void setMandator(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isUser() {
		return false;
	}

	@Override
	public void setUser(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isPatient() {
		return true;
	}

	@Override
	public void setPatient(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isPerson() {
		return true;
	}

	@Override
	public void setPerson(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isLaboratory() {
		return false;
	}

	@Override
	public void setLaboratory(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isOrganization() {
		return false;
	}

	@Override
	public void setOrganization(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getDescription1() {
		return getLastName();
	}

	@Override
	public void setDescription1(String value) {
		setLastName(value);
	}

	@Override
	public String getDescription2() {
		return getFirstName();
	}

	@Override
	public void setDescription2(String value) {
		setFirstName(value);
	}

	@Override
	public String getDescription3() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDescription3(String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getCode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCode(String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public Country getCountry() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCountry(Country value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getZip() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setZip(String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getCity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCity(String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getStreet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStreet(String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getPhone1() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPhone1(String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getPhone2() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPhone2(String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getFax() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFax(String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getEmail() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEmail(String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getWebsite() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWebsite(String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getMobile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMobile(String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getComment() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setComment(String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<IAddress> getAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getGroup() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setGroup(String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getPostalAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPostalAddress(String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public IImage getImage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setImage(IImage value) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<IRelatedContact> getRelatedContacts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isDeceased() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setDeceased(boolean value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getEmail2() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEmail2(String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public IPerson asIPerson() {
		return CoreModelServiceHolder.get().load(this.getId(), IPerson.class).get();
	}

	@Override
	public IPatient asIPatient() {
		return CoreModelServiceHolder.get().load(this.getId(), IPatient.class).get();
	}

	@Override
	public IOrganization asIOrganization() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean addXid(String domain, String id, boolean updateIfExists) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDeleted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setDeleted(boolean value) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getExtInfo(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setExtInfo(Object key, Object value) {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<Object, Object> getMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLabel() {
		return PersonFormatUtil.getPersonalia(this);
	}

	@Override
	public Class<Patient> getFhirType() {
		return Patient.class;
	}

	@Override
	public Class<?> getModelType() {
		return IPatient.class;
	}
}
