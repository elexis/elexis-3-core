package ch.elexis.core.fhir;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.services.ICodeElementService;

public enum CodeSystem {

	/**
	 * {@link ICodeElement} resolvable via {@link ICodeElementService}
	 */
	CODEELEMENT("http://elexis.info/codeelement", StringUtils.EMPTY,
			"An ICodeElement resolvable via ICodeElementService, extends with /codeElementTyp/codeSystemName");

	private String url;
	private String oid;
	private String display;

	CodeSystem(String url, String oid, String display) {
		this.url = url;
		this.oid = oid;
		this.display = display;
	}

	public String getUrl() {
		return url;
	}

	public String getOid() {
		return oid;
	}

	public String getDisplay() {
		return display;
	}
}
