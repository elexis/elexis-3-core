package ch.elexis.core.jpa.entities;

import ch.elexis.core.jpa.entities.converter.AddressTypeConverter;
import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.converter.FuzzyCountryToEnumConverter;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;
import ch.elexis.core.types.AddressType;
import ch.elexis.core.types.Country;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ZUSATZADRESSE")
@EntityListeners(EntityWithIdListener.class)
public class ZusatzAdresse extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@ManyToOne()
	@JoinColumn(name = "Kontakt_ID")
	public Kontakt contact;

	@Column(length = 4, name = "typ")
	@Convert(converter = AddressTypeConverter.class)
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
	public boolean isDeleted() {
		return deleted;
	}

	@Override
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public Long getLastupdate() {
		return lastupdate;
	}

	@Override
	public void setLastupdate(Long lastupdate) {
		this.lastupdate = lastupdate;
	}
}
