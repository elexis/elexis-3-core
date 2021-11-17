package ch.elexis.core.findings.util.fhir;

public enum MedicamentCoding {
		NAME("http://www.elexis.info/medication/name", ""),
		TYPE("http://www.elexis.info/medication/type", ""),
		PHARMACODE("https://index.hcisolutions.ch/DataDoc/element/ARTICLE/ART/PHARMACODE",
			"2.16.756.5.30.2.6.1"),
		ATC("http://www.whocc.no/atc", "2.16.840.1.113883.6.73"),
		GTIN("http://www.gs1.org/gtin", "1.3.160");
	
	private String url;
	private String oid;
	
	MedicamentCoding(String url, String oid){
		this.url = url;
		this.oid = oid;
	}
	
	public String getUrl(){
		return url;
	}
	
	public String getOid(){
		return oid;
	}
}