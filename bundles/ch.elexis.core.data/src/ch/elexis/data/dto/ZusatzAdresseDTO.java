
package ch.elexis.data.dto;

import ch.elexis.core.types.AddressType;

public class ZusatzAdresseDTO {
	private String id = "";
	private String kontaktId = "";
	private String street1 = "";
	private String street2 = "";
	private AddressType addressType = AddressType.PRINCIPAL_RESIDENCE;
	private String place = "";
	private String zip = "";
	private String country = "";
	private String postalAddress = "";
	
	public void setId(String id){
		this.id = id;
	}
	
	public String getId(){
		return id;
	}
	
	public void setStreet1(String street1){
		this.street1 = street1;
	}
	
	public String getStreet1(){
		return street1;
	}
	
	public String getStreet2(){
		return street2;
	}
	
	public void setStreet2(String street2){
		this.street2 = street2;
	}
	
	public AddressType getAddressType(){
		return addressType;
	}
	
	public void setAddressType(AddressType addressType){
		this.addressType = addressType;
	}
	
	public String getPlace(){
		return place;
	}
	
	public void setPlace(String place){
		this.place = place;
	}
	
	public String getZip(){
		return zip;
	}
	
	public void setZip(String zip){
		this.zip = zip;
	}
	
	public String getCountry(){
		return country;
	}
	
	public void setCountry(String country){
		this.country = country;
	}
	
	public void setKontaktId(String kontaktId){
		this.kontaktId = kontaktId;
	}
	
	public String getKontaktId(){
		return kontaktId;
	}
	
	public void setPostalAddress(String postalAddress){
		this.postalAddress = postalAddress;
	}
	
	public String getPostalAddress(){
		return postalAddress;
	}
}
