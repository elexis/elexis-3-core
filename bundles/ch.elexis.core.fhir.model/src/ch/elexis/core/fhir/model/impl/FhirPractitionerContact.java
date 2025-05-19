package ch.elexis.core.fhir.model.impl;

import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.model.Practitioner;

import ch.elexis.core.model.IAddress;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IImage;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IRelatedContact;
import ch.elexis.core.types.Country;

public class FhirPractitionerContact extends AbstractFhirModelAdapter<Practitioner> implements IContact, IMandator {

	public FhirPractitionerContact(Practitioner fhirResource) {
		super(fhirResource);
	}

	@Override
	public boolean isMandator() {
		return true;
	}

	@Override
	public void setMandator(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isUser() {
		return true;
	}

	@Override
	public void setUser(boolean value) {
		throw new UnsupportedOperationException();

	}

	@Override
	public boolean isPatient() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setPatient(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isPerson() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setPerson(boolean value) {
		throw new UnsupportedOperationException();

	}

	@Override
	public boolean isLaboratory() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setLaboratory(boolean value) {
		throw new UnsupportedOperationException();

	}

	@Override
	public boolean isOrganization() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setOrganization(boolean value) {
		throw new UnsupportedOperationException();

	}

	@Override
	public String getDescription1() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDescription1(String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getDescription2() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDescription2(String value) {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPatient asIPatient() {
		// TODO Auto-generated method stub
		return null;
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
	public Class<Practitioner> getFhirType() {
		return Practitioner.class;
	}

	@Override
	public Class<?> getModelType() {
		return IContact.class;
	}

	@Override
	public IContact getBiller() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBiller(IContact value) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setActive(boolean value) {
		// TODO Auto-generated method stub

	}
}
