package ch.elexis.core.findings.util.fhir;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Coding;

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

	public boolean isCodeSystemOf(Coding coding) {
		if (coding != null && StringUtils.isNotBlank(coding.getSystem())) {
			return coding.getSystem().equals(oid) || coding.getSystem().equals(url)
					|| coding.getSystem().equals(getOid());
		}
		return false;
	}
}