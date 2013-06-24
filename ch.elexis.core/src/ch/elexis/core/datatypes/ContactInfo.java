/*******************************************************************************
 * Copyright (c) 2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.datatypes;

public class ContactInfo {
	public enum CTYPE {
		HOME, OFFICE, OTHER
	}
	
	private CTYPE type;
	private String street;
	private String zipcode;
	private String place;
	private String postalSticker;
	private String phone;
	private String fax;
	private String website;
	private String email;
	
	public CTYPE getType(){
		return type;
	}
	
	public void setType(CTYPE type){
		this.type = type;
	}
	
	public String getStreet(){
		return street;
	}
	
	public void setStreet(String street){
		this.street = street;
	}
	
	public String getZipcode(){
		return zipcode;
	}
	
	public void setZipcode(String zipcode){
		this.zipcode = zipcode;
	}
	
	public String getPlace(){
		return place;
	}
	
	public void setPlace(String place){
		this.place = place;
	}
	
	public String getPostalSticker(){
		return postalSticker;
	}
	
	public void setPostalSticker(String postalSticker){
		this.postalSticker = postalSticker;
	}
	
	public String getPhone(){
		return phone;
	}
	
	public void setPhone(String phone){
		this.phone = phone;
	}
	
	public String getFax(){
		return fax;
	}
	
	public void setFax(String fax){
		this.fax = fax;
	}
	
	public String getWebsite(){
		return website;
	}
	
	public void setWebsite(String website){
		this.website = website;
	}
	
	public String getEmail(){
		return email;
	}
	
	public void setEmail(String email){
		this.email = email;
	}
}
