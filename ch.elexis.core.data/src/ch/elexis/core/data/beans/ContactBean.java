/*******************************************************************************
 * Copyright (c) 2013 MEDEVIT <office@medevit.at>.
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
import ch.elexis.core.types.ContactType;
import ch.elexis.core.types.CountryCode;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;

public class ContactBean extends BeanPersistentObject<Kontakt> implements IContact {
	
	public ContactBean(Kontakt kontakt){
		super(kontakt);
	}
	
	@Override
	public ContactType getContactType(){
		if (entity.istOrganisation())
			return ContactType.ORGANIZATION;
		if (entity.istPatient() || entity.istPerson())
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
		return entity.get(Kontakt.FLD_NAME1);
	}
	
	@Override
	public void setDescription1(String description){
		String old = getDescription1();
		entity.set(Kontakt.FLD_NAME1, description);
		firePropertyChange("description1", old, description);
	}
	
	@Override
	public String getDescription2(){
		return entity.get(Kontakt.FLD_NAME2);
	}
	
	@Override
	public void setDescription2(String description){
		String old = getDescription2();
		entity.set(Kontakt.FLD_NAME2, description);
		firePropertyChange("description2", old, description);
	}
	
	@Override
	public String getDescription3(){
		return entity.get(Kontakt.FLD_NAME3);
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
		return entity.get(Kontakt.FLD_IS_MANDATOR).equals(StringConstants.ONE);
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
		return entity.get(Kontakt.FLD_IS_USER).equals(StringConstants.ONE);
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
		return entity.get(Kontakt.FLD_IS_PATIENT).equals(StringConstants.ONE);
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
		return entity.get(Patient.FLD_PATID);
	}
	
	@Override
	public void setCode(String value){
		String old = getCode();
		entity.set(Patient.FLD_PATID, value);
		firePropertyChange("comment", old, value);
	}
	
	@Override
	public CountryCode getCountry(){
		String countryVal = entity.get(Kontakt.FLD_COUNTRY);
		CountryCode ret;
		try {
			ret = CountryCode.get(countryVal);
		} catch (NullPointerException e) {
			ret = CountryCode.NDF;
		}
		return ret;
	}
	
	@Override
	public void setCountry(CountryCode value){
		entity.set(Kontakt.FLD_COUNTRY, value.getLiteral());
	}
}
