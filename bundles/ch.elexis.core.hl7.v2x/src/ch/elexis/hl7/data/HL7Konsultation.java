package ch.elexis.hl7.data;

import java.time.LocalDateTime;

public class HL7Konsultation {
	
	private LocalDateTime zeitpunkt;
	
	private String id;
	
	public LocalDateTime getZeitpunkt(){
		return this.zeitpunkt;
	}
	
	public void setZeitpunkt(LocalDateTime zeitpunkt){
		this.zeitpunkt = zeitpunkt;
	}
	
	public String getId(){
		return id;
	}
	
	public void setId(String id){
		this.id = id;
	}
}
