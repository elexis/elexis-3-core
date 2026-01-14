package ch.elexis.core.findings.codes;

import org.apache.commons.lang3.StringUtils;

public enum MedicamentCoding {
	NAME("http://www.elexis.info/medication/name", StringUtils.EMPTY),
	TYPE("http://www.elexis.info/medication/type", StringUtils.EMPTY),
	PHARMACODE("https://index.hcisolutions.ch/DataDoc/element/ARTICLE/ART/PHARMACODE", "2.16.756.5.30.2.6.1"),
	ATC("http://www.whocc.no/atc", "2.16.840.1.113883.6.73"), GTIN("http://www.gs1.org/gtin", "2.51.1.1");

	private String url;
	private String oid;

	MedicamentCoding(String url, String oid) {
		this.url = url;
		this.oid = oid;
	}

	public String getUrl() {
		return url;
	}

	public String getOid() {
		return "urn:oid:" + oid;
	}

	public boolean isCodeSystemOf(String system) {
		if (system != null && StringUtils.isNotBlank(system)) {
			return system.equals(oid) || system.equals(url) || system.equals(getOid());
		}
		return false;
	}
}