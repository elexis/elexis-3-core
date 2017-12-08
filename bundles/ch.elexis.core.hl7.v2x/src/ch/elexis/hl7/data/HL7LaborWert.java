package ch.elexis.hl7.data;

import java.util.Date;

public class HL7LaborWert {
	private String id;
	private Date zeitpunkt;
	private String resultat;
	private String kommentar;
	private int flags;
	private byte[] docData; // Falls PDF angehängt
	
	public String getId(){
		return id;
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public Date getZeitpunkt(){
		return this.zeitpunkt;
	}
	
	public void setZeitpunkt(Date zeitpunkt){
		this.zeitpunkt = zeitpunkt;
	}
	
	public String getResultat(){
		return resultat;
	}
	
	public void setResultat(String resultat){
		this.resultat = resultat;
	}
	
	public byte[] getDocData(){
		return docData;
	}
	
	public void setDocData(byte[] docData){
		this.docData = docData;
	}
	
	public void setFlags(int flags){
		this.flags = flags;
	}
	
	public int getFlags(){
		return this.flags;
	}
	
	public String getKommentar(){
		return kommentar;
	}
	
	public void setKommentar(String kommentar){
		this.kommentar = kommentar;
	}
	
}
