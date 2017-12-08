package ch.elexis.hl7.data;

public abstract class HL7Kontakt {
	private String title;
	private String name;
	private String firstname;
	private String address1;
	private String address2;
	private String city;
	private String zip;
	private String country;
	private String phone1;
	private String phone2;
	private String email;
	private String fax;
	
	public HL7Kontakt(){
		super();
	}
	
	public String getTitle(){
		return title;
	}
	
	public void setTitle(String title){
		this.title = title;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getFirstname(){
		return firstname;
	}
	
	public void setFirstname(String firstname){
		this.firstname = firstname;
	}
	
	public String getAddress1(){
		return address1;
	}
	
	public void setAddress1(String address1){
		this.address1 = address1;
	}
	
	public String getAddress2(){
		return address2;
	}
	
	public void setAddress2(String address2){
		this.address2 = address2;
	}
	
	public String getCity(){
		return city;
	}
	
	public void setCity(String city){
		this.city = city;
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
	
	public String getPhone1(){
		return phone1;
	}
	
	public void setPhone1(String phone1){
		this.phone1 = phone1;
	}
	
	public String getPhone2(){
		return phone2;
	}
	
	public void setPhone2(String phone2){
		this.phone2 = phone2;
	}
	
	public String getEmail(){
		return email;
	}
	
	public void setEmail(String email){
		this.email = email;
	}
	
	public String getFax(){
		return fax;
	}
	
	public void setFax(String fax){
		this.fax = fax;
	}
}
