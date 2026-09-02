package ch.elexis.core.fhir.model.impl;

import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.model.Practitioner;

import ch.elexis.core.model.IAddress;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IRelatedContact;
import ch.elexis.core.model.Identifiable;

public class FhirPractitioner extends AbstractFhirContactModelAdapter<IMandator, Practitioner>
		implements IContact, IMandator {

	public FhirPractitioner(Practitioner fhirResource) {
		super(fhirResource);
	}

	@Override
	public Class<Practitioner> getFhirType() {
		return Practitioner.class;
	}

	@Override
	public Class<? extends Identifiable> getModelType() {
		return IMandator.class;
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
	public List<IAddress> getAddress() {
		// TODO Auto-generated method stub
		return null;
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
