package ch.elexis.core.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.converter.FuzzyCountryToEnumConverter;
import ch.elexis.core.types.AddressType;
import ch.elexis.core.types.Country;

@Entity
@Table(name = "ZUSATZADRESSE")
public class ZusatzAdresse extends AbstractDBObjectIdDeleted {

	@JoinColumn(name = "Kontakt_ID")
	public Kontakt contact;

	@Column(length = 4, name = "typ")
	public AddressType addressType;

	@Column(length = 255, name = "strasse1")
	public String street1;

	@Column(length = 255, name = "strasse2")
	public String street2;

	@Column(length = 6, name = "plz")
	public String zip;

	@Column(length = 255, name = "ort")
	public String city;

	@Column(length = 255, name = "land")
	@Convert(converter = FuzzyCountryToEnumConverter.class)
	public Country country;

	@Lob
	@Column(name = "anschrift")
	public String writtenAddress;

	public Kontakt getContact() {
		return contact;
	}

	public void setContact(Kontakt contact) {
		this.contact = contact;
	}

	public AddressType getAddressType() {
		return addressType;
	}

	public void setAddressType(AddressType addressType) {
		this.addressType = addressType;
	}

	public String getStreet1() {
		return street1;
	}

	public void setStreet1(String street1) {
		this.street1 = street1;
	}

	public String getStreet2() {
		return street2;
	}

	public void setStreet2(String street2) {
		this.street2 = street2;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public Country getCountry() {
		return country;
	}
	
	public void setCountry(Country country) {
		this.country = country;
	}

	public String getWrittenAddress() {
		return writtenAddress;
	}

	public void setWrittenAddress(String writtenAddress) {
		this.writtenAddress = writtenAddress;
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}
}
