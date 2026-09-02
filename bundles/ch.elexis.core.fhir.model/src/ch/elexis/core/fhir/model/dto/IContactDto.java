package ch.elexis.core.fhir.model.dto;

import java.util.List;

import ch.elexis.core.model.IAddress;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IImage;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IRelatedContact;
import ch.elexis.core.types.Country;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IContactDto extends IdentifiableDeletableDto implements IContact {

	boolean organization;
	boolean laboratory;
	boolean person;
	boolean patient;
	boolean mandator;
	boolean user;

	String description1;
	String description2;
	String description3;
	String code;
	String email;
	String website;
	String postalAddress;
	String zip;
	boolean deceased;
	String city;
	String fax;
	String phone1;
	String phone2;
	String mobile;
	String email2;
	String street;
	Country country;
	String comment;

	String group;
	IImage image;

	@Override
	public IPerson asIPerson() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IOrganization asIOrganization() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPatient asIPatient() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IRelatedContact> getRelatedContacts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IAddress> getAddress() {
		// TODO Auto-generated method stub
		return null;
	}
}
