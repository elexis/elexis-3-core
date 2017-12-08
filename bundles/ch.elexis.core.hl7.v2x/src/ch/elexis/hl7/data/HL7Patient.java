package ch.elexis.hl7.data;

import java.util.Date;

public class HL7Patient extends HL7Kontakt {
	private String patCode;
	private Boolean isMale;
	private Date birthdate;
	
	public HL7Patient(){
		super();
	}
	
	public String getPatCode(){
		return patCode;
	}
	
	public void setPatCode(String patCode){
		this.patCode = patCode;
	}
	
	public Boolean isMale(){
		return this.isMale;
	}
	
	public void setIsMale(Boolean male){
		this.isMale = male;
	}
	
	public Date getBirthdate(){
		return birthdate;
	}
	
	public void setBirthdate(Date birthdate){
		this.birthdate = birthdate;
	}
}
