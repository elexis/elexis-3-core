/*******************************************************************************
 * Copyright (c) 2014 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.data.beans;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.beans.base.BeanPersistentObject;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IUser;
import ch.elexis.core.types.ContactType;
import ch.elexis.core.types.Country;
import ch.elexis.core.types.Gender;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Labor;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Person;
import ch.rgw.tools.TimeTool;

public class ContactBean extends BeanPersistentObject<Kontakt> implements IContact, IPerson,
		IPatient, IUser {
	
	private ContactCache cache;
	
	public ContactBean(Kontakt kontakt){
		super(kontakt);
		cache = new ContactCache();
	}
	
	public Patient getAsPatientEntity() {
		return (Patient) entity;
	}
	
	public Labor getAsLaboratoryEntity() {
		return (Labor) entity;
	}
	
	@Override
	protected void updateCache(){
		cache = new ContactCache();
	}
	
	@Override
	public ContactType getContactType(){
		if (cache.isOrganization)
			return ContactType.ORGANIZATION;
		if (cache.isPatient || cache.isPerson)
			return ContactType.PERSON;
		return ContactType.UNKNOWN;
	}
	
	@Override
	public void setContactType(ContactType type){
		ContactType old = getContactType();
		switch (type) {
		case PERSON:
			entity.set(Kontakt.FLD_IS_PERSON, StringConstants.ONE);
			entity.set(Kontakt.FLD_IS_ORGANIZATION, StringConstants.ZERO);
			break;
		case ORGANIZATION:
			entity.set(Kontakt.FLD_IS_ORGANIZATION, StringConstants.ONE);
			entity.set(Kontakt.FLD_IS_PERSON, StringConstants.ZERO);
			break;
		case UNKNOWN:
			entity.set(Kontakt.FLD_IS_ORGANIZATION, StringConstants.ZERO);
			entity.set(Kontakt.FLD_IS_PERSON, StringConstants.ZERO);
			break;
		default:
			break;
		}
		firePropertyChange("contactType", old, type);
	}
	
	@Override
	public String getDescription1(){
		return cache.description1;
	}
	
	@Override
	public void setDescription1(String description){
		String old = getDescription1();
		entity.set(Kontakt.FLD_NAME1, description);
		firePropertyChange("description1", old, description);
	}
	
	@Override
	public String getDescription2(){
		return cache.description2;
	}
	
	@Override
	public void setDescription2(String description){
		String old = getDescription2();
		entity.set(Kontakt.FLD_NAME2, description);
		firePropertyChange("description2", old, description);
	}
	
	@Override
	public String getDescription3(){
		return cache.description3;
	}
	
	@Override
	public void setDescription3(String description){
		String old = getDescription3();
		entity.set(Kontakt.FLD_NAME3, description);
		firePropertyChange("description3", old, description);
	}
	
	@Override
	public String getZip(){
		return entity.get(Kontakt.FLD_ZIP);
	}
	
	@Override
	public void setZip(String zip){
		String old = getZip();
		entity.set(Kontakt.FLD_ZIP, zip);
		firePropertyChange("zip", old, zip);
	}
	
	@Override
	public String getCity(){
		return entity.get(Kontakt.FLD_PLACE);
	}
	
	@Override
	public void setCity(String city){
		String old = getCity();
		entity.set(Kontakt.FLD_PLACE, city);
		firePropertyChange("city", old, city);
	}
	
	@Override
	public String getStreet(){
		return entity.get(Kontakt.FLD_STREET);
	}
	
	@Override
	public void setStreet(String street){
		String old = getStreet();
		entity.set(Kontakt.FLD_STREET, street);
		firePropertyChange("street", old, street);
	}
	
	@Override
	public String getPhone1(){
		return entity.get(Kontakt.FLD_PHONE1);
	}
	
	@Override
	public void setPhone1(String phone){
		String old = getPhone1();
		entity.set(Kontakt.FLD_PHONE1, phone);
		firePropertyChange("phone1", old, phone);
	}
	
	@Override
	public String getPhone2(){
		return entity.get(Kontakt.FLD_PHONE2);
	}
	
	@Override
	public void setPhone2(String phone){
		String old = getPhone2();
		entity.set(Kontakt.FLD_PHONE2, phone);
		firePropertyChange("phone2", old, phone);
	}
	
	@Override
	public String getFax(){
		return entity.get(Kontakt.FLD_FAX);
	}
	
	@Override
	public void setFax(String fax){
		String old = getFax();
		entity.set(Kontakt.FLD_FAX, fax);
		firePropertyChange("fax", old, fax);
	}
	
	@Override
	public String getEmail(){
		return entity.get(Kontakt.FLD_E_MAIL);
	}
	
	@Override
	public void setEmail(String email){
		String old = getEmail();
		entity.set(Kontakt.FLD_E_MAIL, email);
		firePropertyChange("email", old, email);
	}
	
	@Override
	public String getWebsite(){
		return entity.get(Kontakt.FLD_WEBSITE);
	}
	
	@Override
	public void setWebsite(String website){
		String old = getWebsite();
		entity.set(Kontakt.FLD_WEBSITE, website);
		firePropertyChange("website", old, website);
	}
	
	@Override
	public String getMobile(){
		return entity.get(Kontakt.FLD_MOBILEPHONE);
	}
	
	@Override
	public void setMobile(String mobile){
		String old = getMobile();
		entity.set(Kontakt.FLD_MOBILEPHONE, mobile);
		firePropertyChange("mobile", old, mobile);
	}
	
	@Override
	public String getComment(){
		return entity.getBemerkung();
	}
	
	@Override
	public void setComment(String comment){
		String old = getComment();
		entity.setBemerkung(comment);
		firePropertyChange("comment", old, comment);
	}
	
	@Override
	public String getId(){
		return entity.getId();
	}
	
	@Override
	public String getLabel(){
		return getDescription1() + " " + getDescription2() + ", " + getStreet() + ", " + getZip()
			+ " " + getCity();
	}
	
	@Override
	public boolean isMandator(){
		return cache.isMandator;
	}
	
	@Override
	public void setMandator(boolean value){
		boolean old = isMandator();
		entity.set(Kontakt.FLD_IS_MANDATOR, (value == true) ? StringConstants.ONE
				: StringConstants.ZERO);
		firePropertyChange("mandator", old, value);
	}
	
	@Override
	public boolean isUser(){
		return cache.isUser;
	}
	
	@Override
	public void setUser(boolean value){
		boolean old = isUser();
		entity.set(Kontakt.FLD_IS_USER, (value == true) ? StringConstants.ONE
				: StringConstants.ZERO);
		firePropertyChange("user", old, value);
	}
	
	@Override
	public boolean isPatient(){
		return cache.isPatient;
	}
	
	@Override
	public void setPatient(boolean value){
		boolean old = isPatient();
		entity.set(Kontakt.FLD_IS_PATIENT, (value == true) ? StringConstants.ONE
				: StringConstants.ZERO);
		firePropertyChange("patient", old, value);
	}
	
	@Override
	public String getCode(){
		return cache.code;
	}
	
	@Override
	public void setCode(String value){
		String old = getCode();
		entity.set(Patient.FLD_PATID, value);
		firePropertyChange("code", old, value);
	}
	
	@Override
	public Country getCountry(){
		String countryVal = entity.get(Kontakt.FLD_COUNTRY);
		Country ret;
		try {
			ret = Country.fromValue(countryVal);
		} catch (NullPointerException | IllegalArgumentException e) {
			ret = Country.NDF;
		}
		return ret;
	}
	
	@Override
	public void setCountry(Country value){
		Country old = getCountry();
		entity.set(Kontakt.FLD_COUNTRY, value.value());
		firePropertyChange("country", old, value);
	}
	
	@Override
	public boolean isDeleted(){
		return cache.isDeleted;
	}
	
	@Override
	public void setDeleted(boolean value){
		boolean old = isDeleted();
		entity.delete();
		firePropertyChange("deleted", old, value);
	}
	
	// Person
	
	@Override
	public TimeTool getDateOfBirth(){
		return cache.dateOfBirth;
	}
	
	@Override
	public void setDateOfBirth(TimeTool value){
		TimeTool old = getDateOfBirth();
		entity.set(Person.BIRTHDATE, value.toString(TimeTool.DATE_COMPACT));
		firePropertyChange("dateOfBirth", old, value);
	}
	
	@Override
	public Gender getGender(){
		return cache.sex;
	}
	
	@Override
	public void setGender(Gender value){
		Gender old = getGender();
		String vs;
		switch (value) {
		case MALE:
			vs = "m";
			break;
		case FEMALE:
			vs = "w";
			break;
		case UNDEFINED:
			vs = "u";
			break;
		default:
			vs = "";
		}
		entity.set(Person.SEX, vs);
		firePropertyChange("gender", old, value);
	}
	
	@Override
	public String getTitel(){
		return cache.titel;
	}
	
	@Override
	public void setTitel(String value){
		String old = getTitel();
		entity.set(Person.TITLE, value);
		firePropertyChange("titel", old, value);
	}
	
	@Override
	public String getTitelSuffix(){
		return cache.titelSuffix;
	}
	
	@Override
	public void setTitelSuffix(String value){
		String old = getTitel();
		entity.set(Person.FLD_TITLE_SUFFIX, value);
		firePropertyChange("titelSuffix", old, value);
	}
	
	// Patient -----------------------
	
	@Override
	public String getDiagnosen(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setDiagnosen(String value){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getRisk(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setRisk(String value){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getFamilyAnamnese(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setFamilyAnamnese(String value){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getPersonalAnamnese(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setPersonalAnamnese(String value){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getAllergies(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setAllergies(String value){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getPatientLabel(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getPatientNr(){
		return getCode();
	}
	
	@Override
	public void setPatientNr(String value){
		setCode(value);
	}
	
	// User ---
	@Override
	public String getUsername(){
		return cache.description3;
	}
	
	@Override
	public void setUsername(String value){
		String old = getUsername();
		entity.set(Kontakt.FLD_NAME3, value);
		firePropertyChange("username", old, value);
	}
	
	/**
	 * This class caches contact properties to speed up the current {@link PersistentObject}
	 * dependent implementation, where every access is executed synchronous to the DB.
	 */
	private class ContactCache {
		boolean isDeleted, isPerson, isOrganization, isMandator, isUser, isPatient;
		String code, description1, description2, description3, titel, titelSuffix;
		TimeTool dateOfBirth;
		Gender sex;
		
		public ContactCache(){
			// make sure Patient field mapping is initialized
			if (PersistentObject.map(Patient.TABLENAME, Patient.FLD_PATID)
				.startsWith(PersistentObject.MAPPING_ERROR_MARKER)) {
				Patient.load("0");
			}
			
			String[] labels = new String[14];
			entity.get(new String[] {
				Kontakt.FLD_DELETED, Kontakt.FLD_IS_PERSON, Kontakt.FLD_IS_ORGANIZATION,
				Kontakt.FLD_IS_MANDATOR, Kontakt.FLD_IS_USER, Kontakt.FLD_IS_PATIENT,
				Kontakt.FLD_NAME1, Kontakt.FLD_NAME2, Kontakt.FLD_NAME3, Person.BIRTHDATE,
				Person.SEX, Patient.FLD_PATID, Person.TITLE, Person.FLD_TITLE_SUFFIX
			}, labels);
			
			isDeleted = labels[0].equals(StringConstants.ONE);
			isPerson = labels[1].equals(StringConstants.ONE);
			isOrganization = labels[2].equals(StringConstants.ONE);
			isMandator = labels[3].equals(StringConstants.ONE);
			isUser = labels[4].equals(StringConstants.ONE);
			isPatient = labels[5].equals(StringConstants.ONE);
			description1 = labels[6];
			description2 = labels[7];
			description3 = labels[8];
			dateOfBirth = new TimeTool(labels[9]);
			sex = switchSex(labels[10]);
			code = labels[11];
			titel = labels[12];
			titelSuffix = labels[13];
		}
		
		private Gender switchSex(String labels){
			if (labels == null || labels.length() < 1)
				return Gender.UNKNOWN;
			switch (labels.toLowerCase().charAt(0)) {
			case 'w':
				return Gender.FEMALE;
			case 'f':
				return Gender.FEMALE;
			case 'u':
				return Gender.UNDEFINED;
			case 'm':
				return Gender.MALE;
			default:
				return Gender.UNKNOWN;
			}
		}
	}

	@Override
	public String getPassword(){
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPassword(String value){
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getFirstName(){
		return getDescription2();
	}

	@Override
	public String getFamilyName(){
		return getDescription1();
	}
}
