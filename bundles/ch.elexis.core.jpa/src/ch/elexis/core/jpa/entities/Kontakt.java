/*******************************************************************************
 * Copyright (c) 2016 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.jpa.entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.CacheType;
import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;

import ch.elexis.core.jpa.entities.converter.FuzzyCountryToEnumConverter;
import ch.elexis.core.jpa.entities.converter.FuzzyGenderToEnumConverter;
import ch.elexis.core.jpa.entities.listener.KontaktEntityListener;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.types.ContactType;
import ch.elexis.core.types.Country;
import ch.elexis.core.types.Gender;
import ch.rgw.tools.TimeTool;


/**
 * The persistent class for the Elexis KONTAKT database table. Valid from DB
 * Version 3.1.0
 * 
 * @author M. Descher, MEDEVIT, Austria
 */
@Entity
@Table(name = "KONTAKT")
@XmlRootElement(name = "contact")
@Cache(type = CacheType.NONE)
@EntityListeners(KontaktEntityListener.class)
public class Kontakt extends AbstractDBObjectIdDeletedExtInfo implements Serializable, IPatient {
	protected static final long serialVersionUID = 1L;

	@Basic(fetch = FetchType.LAZY)
	@Lob()
	@Column(name = "allergien")
	protected String allergies;

	@Lob()
	protected String anschrift;

	@Lob()
	@Column(name = "bemerkung")
	protected String comment;

	@Column(length = 255, name = "bezeichnung1")
	protected String description1;

	@Column(length = 255, name = "bezeichnung2")
	protected String description2;

	/**
	 * Contains the following values in the respective instantiations of contact
	 * isIstPatient(): ? isIstPerson(): if medic: area of expertise isIstMandant():
	 * username/mandant short name isIstAnwender(): username/mandant short name
	 * isIstOrganisation(): contact person isIstLabor(): ?
	 */
	@Column(length = 255, name = "bezeichnung3")
	protected String description3;

	@Basic(fetch = FetchType.LAZY)
	@Convert(value = "ElexisDBCompressedStringConverter")
	@Column(columnDefinition = "BLOB")
	protected String diagnosen;

	@Column(length = 255)
	protected String email;

	// @Basic(fetch = FetchType.LAZY)
	@Convert(value = "ElexisDBCompressedStringConverter")
	@Column(name = "famAnamnese", columnDefinition = "BLOB")
	protected String familyAnamnese;

	// @Basic(fetch = FetchType.LAZY)
	@Convert(value = "ElexisDBCompressedStringConverter")
	@Column(name = "persAnamnese", columnDefinition = "BLOB")
	protected String personalAnamnese;

	@Column(length = 30)
	protected String fax;

	@Column(name = "geburtsdatum", length = 8)
	protected LocalDate dob;

	@Converter(name = "FuzzyGenderToEnumConverter", converterClass = FuzzyGenderToEnumConverter.class)
	@Convert("FuzzyGenderToEnumConverter")
	@Column(name = "geschlecht")
	protected Gender gender;

	@Column(length = 10)
	protected String gruppe;

	@Convert("booleanStringConverter")
	@Column(name = "istPerson")
	protected boolean person;

	@Convert("booleanStringConverter")
	@Column(name = "istPatient")
	protected boolean patient;

	@Convert("booleanStringConverter")
	@Column(name = "istAnwender")
	protected boolean user;

	@Convert("booleanStringConverter")
	@Column(name = "istMandant")
	protected boolean mandator;

	@Convert("booleanStringConverter")
	@Column(name = "istOrganisation")
	protected boolean organisation;

	@Convert("booleanStringConverter")
	@Column(name = "istLabor")
	protected boolean laboratory;

	@Column(length = 3, name = "land")
	@Converter(name = "FuzzyCountryToEnumConverter", converterClass = FuzzyCountryToEnumConverter.class)
	@Convert(value = "FuzzyCountryToEnumConverter")
	protected Country country;

	@Column(length = 30, name = "natelNr")
	protected String mobile;

	@Column(length = 255, name = "ort")
	protected String city;

	/**
	 * Contains according to contact-type manifestation:<br>
	 * isPatient: patientNr<br>
	 * isOrganization /<br>
	 * isPerson: ID
	 */
	@Column(length = 40, name = "patientNr")
	protected String code;

	@Column(length = 6, name = "plz")
	protected String zip;

	@Basic(fetch = FetchType.LAZY)
	@Lob()
	@Column(name = "risiken")
	protected String risk;

	@Column(length = 255, name = "strasse")
	protected String street;

	@Basic(fetch = FetchType.LAZY)
	@Lob()
	protected byte[] sysAnamnese;

	@Column(length = 30, name = "telefon1")
	protected String phone1;

	@Column(length = 30, name = "telefon2")
	protected String phone2;

	@Column(length = 255)
	protected String titel;

	@Column(length = 255)
	protected String titelSuffix;

	@Column(length = 255)
	protected String website;

	/**
	 * All related {@link Fall} entities; modifications ignored
	 */
	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "PatientID", updatable = false, insertable = false, nullable = false)
	protected List<Fall> faelle = new ArrayList<>();

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "owner")
	protected List<Userconfig> userconfig = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "contact", orphanRemoval = true)
	@MapKey(name = "id")
	protected Map<String, ZusatzAdresse> addresses = new HashMap<>();

	/**
	 * Contacts we relate to (egress reference)
	 */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "myKontakt")
	@MapKey(name = "id")
	protected Map<String, KontaktAdressJoint> relatedContacts = new HashMap<>();

	/**
	 * Contacts we are related by (ingress reference); modifications ignored<br>
	 * <b>ATTENTION</b> In these relationships, the <i>other</i> contact is
	 * referenced via {@link KontaktAdressJoint#getMyKontakt()} as we are the
	 * {@link KontaktAdressJoint#getOtherKontakt()}
	 */
	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "otherID", updatable = false, insertable = false, nullable = false)
	protected Collection<KontaktAdressJoint> relatedByContacts;

	// ---------------------------------------------
	public Kontakt() {
	}

	public String getLabel() {
		return getDescription1() + "," + getDescription2() + "," + getDescription3();
	}

	public String getAnschrift() {
		return anschrift;
	}

	public void setAnschrift(String anschrift) {
		this.anschrift = anschrift;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}

	public String getDiagnosen() {
		return diagnosen;
	}

	public void setDiagnosen(String diagnosen) {
		this.diagnosen = diagnosen;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFamilyAnamnese() {
		return familyAnamnese;
	}

	public void setFamilyAnamnese(String familyAnamnese) {
		this.familyAnamnese = familyAnamnese;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public LocalDate getDob() {
		return dob;
	}

	public void setDob(LocalDate dob) {
		this.dob = dob;
	}

	public String getGruppe() {
		return gruppe;
	}

	public void setGruppe(String gruppe) {
		this.gruppe = gruppe;
	}

	public boolean isUser() {
		return user;
	}

	public void setUser(boolean user) {
		this.user = user;
	}

	public boolean isLaboratory() {
		return laboratory;
	}

	public void setLaboratory(boolean laboratory) {
		this.laboratory = laboratory;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getRisk() {
		return risk;
	}

	public void setRisk(String risk) {
		this.risk = risk;
	}

	public byte[] getSysAnamnese() {
		return sysAnamnese;
	}

	public void setSysAnamnese(byte[] sysAnamnese) {
		this.sysAnamnese = sysAnamnese;
	}

	public String getTitel() {
		return titel;
	}

	public void setTitel(String titel) {
		this.titel = titel;
	}

	public String getTitelSuffix() {
		return titelSuffix;
	}

	public void setTitelSuffix(String titelSuffix) {
		this.titelSuffix = titelSuffix;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public List<Fall> getFaelle() {
		return faelle;
	}

	public void setFaelle(List<Fall> faelle) {
		this.faelle = faelle;
	}

	public Map<String, ZusatzAdresse> getAddresses() {
		return addresses;
	}

	public void setAddresses(Map<String, ZusatzAdresse> addresses) {
		this.addresses = addresses;
	}

	public Map<String, KontaktAdressJoint> getRelatedContacts() {
		return relatedContacts;
	}
	
	public void setRelatedContacts(Map<String, KontaktAdressJoint> relatedContacts) {
		this.relatedContacts = relatedContacts;
	}

	public Collection<KontaktAdressJoint> getRelatedByContacts() {
		return relatedByContacts;
	}

	public void setRelatedByContacts(Collection<KontaktAdressJoint> relatedByContacts) {
		this.relatedByContacts = relatedByContacts;
	}

	public List<Userconfig> getUserconfig() {
		return userconfig;
	}

	public void setUserconfig(List<Userconfig> userconfig) {
		this.userconfig = userconfig;
	}

	public String getPhone2() {
		return phone2;
	}

	public void setPhone2(String phone2) {
		this.phone2 = phone2;
	}

	public String getDescription1() {
		return description1;
	}

	public void setDescription1(String description1) {
		this.description1 = description1;
	}

	public String getDescription2() {
		return description2;
	}

	public void setDescription2(String description2) {
		this.description2 = description2;
	}

	public String getDescription3() {
		return description3;
	}

	public void setDescription3(String description3) {
		this.description3 = description3;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getPhone1() {
		return phone1;
	}

	public void setPhone1(String phone1) {
		this.phone1 = phone1;
	}

	public boolean isPerson() {
		return person;
	}

	public void setPerson(boolean person) {
		this.person = person;
	}

	public boolean isPatient() {
		return patient;
	}

	public void setPatient(boolean patient) {
		this.patient = patient;
	}

	public boolean isMandator() {
		return mandator;
	}

	public void setMandator(boolean mandator) {
		this.mandator = mandator;
	}

	public boolean isOrganisation() {
		return organisation;
	}

	public void setOrganisation(boolean organisation) {
		this.organisation = organisation;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	@Override
	@Transient
	public ContactType getContactType() {
		if (isOrganisation()) {
			if (isLaboratory()) {
				return ContactType.LABORATORY;
			}
			return ContactType.ORGANIZATION;
		} else {
			if (isPatient()) {
				return ContactType.PATIENT;
			}
			return ContactType.PERSON;
		}
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public String getAllergies() {
		return allergies;
	}

	public void setAllergies(String allergies) {
		this.allergies = allergies;
	}

	public String getPersonalAnamnese() {
		return personalAnamnese;
	}

	public void setPersonalAnamnese(String personalAnamnese) {
		this.personalAnamnese = personalAnamnese;
	}

	@Override
	@Transient
	public void setContactType(ContactType value) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Transient
	public TimeTool getDateOfBirth() {
		return new TimeTool(getDob());
	}

	@Override
	@Transient
	public void setDateOfBirth(TimeTool value) {
		setDob(value.toLocalDate());
	}

	@Override
	@Transient
	public String getFirstName() {
		return getDescription2();
	}

	@Override
	@Transient
	public String getFamilyName() {
		return getDescription1();
	}

	@Override
	@Transient
	public String getPatientNr() {
		return getCode();
	}

	@Override
	@Transient
	public void setPatientNr(String patientNr) {
		setCode(patientNr);
	}

	@Override
	@Transient
	public String getPatientLabel() {
		return getLabel();
	}
	
	@Transient
	public long getAgeAt(LocalDateTime dateTime, ChronoUnit chronoUnit){
		LocalDateTime birthDateTime = new TimeTool(getDateOfBirth()).toLocalDateTime();
		return chronoUnit.between(birthDateTime, dateTime);
	}
}
