package ch.elexis.hl7.data;

public class HL7Kostentraeger extends HL7Kontakt {
	
	private String ean;
	
	public HL7Kostentraeger(){
		super();
	}
	
	public String getEan(){
		return ean;
	}
	
	public void setEan(String ean){
		this.ean = ean;
	}
}
