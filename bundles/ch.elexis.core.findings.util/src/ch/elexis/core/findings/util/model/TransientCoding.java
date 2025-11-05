package ch.elexis.core.findings.util.model;

import java.util.Objects;

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

	@Override
	public int hashCode() {
		return Objects.hash(code, system);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TransientCoding other = (TransientCoding) obj;
		return Objects.equals(code, other.code) && Objects.equals(system, other.system);
	}
}
