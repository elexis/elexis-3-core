package ch.elexis.core.findings.util.model;

import org.apache.commons.lang3.StringUtils;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IObservation.ObservationCode;

public class TransientCoding implements ICoding {

	private String system;
	private String code;
	private String display;

	public TransientCoding(String system, String code, String display) {
		this.system = system;
		this.code = code;
		this.display = display;
	}

	public TransientCoding(ObservationCode code) {
		this.system = code.getIdentifierSystem().getSystem();
		this.code = code.getCode();
		this.display = StringUtils.EMPTY;
	}

	@Override
	public String getSystem() {
		return system;
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getDisplay() {
		return display;
	}

}
