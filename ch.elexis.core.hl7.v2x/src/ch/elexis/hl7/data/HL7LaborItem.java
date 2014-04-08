package ch.elexis.hl7.data;

public class HL7LaborItem {
	private String id;
	private String kuerzel;
	private String titel;
	private String refMann;
	private String refFrau;
	private String einheit;
	private String gruppe;
	private String prio;
	private Typ typ;
	
	public enum Typ {
		NUMERIC, TEXT, ABSOLUTE, FORMULA, DOCUMENT
	};
	
	public String getId(){
		return id;
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public String getKuerzel(){
		return kuerzel;
	}
	
	public void setKuerzel(String kuerzel){
		this.kuerzel = kuerzel;
	}
	
	public String getTitel(){
		return titel;
	}
	
	public void setTitel(String titel){
		this.titel = titel;
	}
	
	public String getRefMann(){
		return refMann;
	}
	
	public void setRefMann(String refMann){
		this.refMann = refMann;
	}
	
	public String getRefFrau(){
		return refFrau;
	}
	
	public void setRefFrau(String refFrau){
		this.refFrau = refFrau;
	}
	
	public String getEinheit(){
		return einheit;
	}
	
	public void setEinheit(String einheit){
		this.einheit = einheit;
	}
	
	public String getGruppe(){
		return gruppe;
	}
	
	public void setGruppe(String gruppe){
		this.gruppe = gruppe;
	}
	
	public String getPrio(){
		return prio;
	}
	
	public void setPrio(String prio){
		this.prio = prio;
	}
	
	public Typ getTyp(){
		return typ;
	}
	
	public void setTyp(Typ typ){
		this.typ = typ;
	}
}
