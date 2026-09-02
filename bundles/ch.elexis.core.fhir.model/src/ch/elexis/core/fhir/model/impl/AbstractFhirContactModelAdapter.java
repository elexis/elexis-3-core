package ch.elexis.core.fhir.model.impl;

import java.util.List;

import org.hl7.fhir.r4.model.DomainResource;

import ch.elexis.core.model.IAddress;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IImage;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IRelatedContact;
import ch.elexis.core.types.Country;

public abstract class AbstractFhirContactModelAdapter<T extends IContact, U extends DomainResource>
		extends AbstractFhirModelAdapter<T, U> implements IContact {

	public AbstractFhirContactModelAdapter(U fhirResource) {
		super(fhirResource);
	}

	@Override
	public boolean isMandator() {
		return getLoaded().isMandator();
	}

	@Override
	public void setMandator(boolean value) {
		getLoadedMarkDirty().setMandator(value);
	}

	@Override
	public boolean isUser() {
		return getLoaded().isUser();
	}

	@Override
	public void setUser(boolean value) {
		getLoadedMarkDirty().setUser(value);
	}

	@Override
	public boolean isPerson() {
		return getLoaded().isPerson();
	}

	@Override
	public void setPerson(boolean value) {
		getLoadedMarkDirty().setPerson(value);
	}

	@Override
	public boolean isPatient() {
		return getLoaded().isPatient();
	}

	@Override
	public void setPatient(boolean value) {
		getLoadedMarkDirty().setPatient(value);
	}

	@Override
	public boolean isOrganization() {
		return getLoaded().isOrganization();
	}

	@Override
	public void setOrganization(boolean value) {
		getLoadedMarkDirty().setOrganization(value);
	}

	@Override
	public boolean isLaboratory() {
		return getLoaded().isLaboratory();
	}

	@Override
	public void setLaboratory(boolean value) {
		getLoadedMarkDirty().setLaboratory(value);
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
	public boolean isDeleted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setDeleted(boolean value) {
		// TODO Auto-generated method stub
	}

	@Override
	public String getDescription1() {
		return getLoaded().getDescription1();
	}

	@Override
	public void setDescription1(String value) {
		getLoadedMarkDirty().setDescription1(value);
	}

	@Override
	public String getDescription2() {
		return getLoaded().getDescription2();
	}

	@Override
	public void setDescription2(String value) {
		getLoadedMarkDirty().setDescription2(value);
	}

	@Override
	public String getDescription3() {
		return getLoaded().getDescription3();
	}

	@Override
	public void setDescription3(String value) {
		getLoadedMarkDirty().setDescription3(value);
	}

	@Override
	public Country getCountry() {
		return getLoaded().getCountry();
	}

	@Override
	public void setCountry(Country value) {
		getLoadedMarkDirty().setCountry(value);
	}

	@Override
	public String getCity() {
		return getLoaded().getCity();
	}

	@Override
	public void setCity(String value) {
		getLoadedMarkDirty().setCity(value);

	}

	@Override
	public String getStreet() {
		return getLoaded().getStreet();
	}

	@Override
	public void setStreet(String value) {
		getLoadedMarkDirty().setStreet(value);
	}

	@Override
	public String getZip() {
		return getLoaded().getZip();
	}

	@Override
	public void setZip(String value) {
		getLoadedMarkDirty().setZip(value);
	}

	@Override
	public String getPhone1() {
		return getLoaded().getPhone1();
	}

	@Override
	public void setPhone1(String value) {
		getLoadedMarkDirty().setPhone1(value);
	}

	@Override
	public String getPhone2() {
		return getLoaded().getPhone2();
	}

	@Override
	public void setPhone2(String value) {
		getLoadedMarkDirty().setPhone2(value);
	}

	@Override
	public String getFax() {
		return getLoaded().getFax();
	}

	@Override
	public void setFax(String value) {
		getLoadedMarkDirty().setFax(value);
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
	public String getEmail2() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEmail2(String value) {
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
		return getLoaded().getComment();
	}

	@Override
	public void setComment(String value) {
		getLoadedMarkDirty().setComment(value);
	}

	@Override
	public String getCode() {
		return getLoaded().getCode();
	}

	@Override
	public void setCode(String value) {
		getLoadedMarkDirty().setCode(value);
	}

	@Override
	public List<IAddress> getAddress() {
		// TODO Auto-generated method stub
		return null;
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
	public String getGroup() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setGroup(String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<IRelatedContact> getRelatedContacts() {
		// TODO Auto-generated method stub
		return null;
	}

}
