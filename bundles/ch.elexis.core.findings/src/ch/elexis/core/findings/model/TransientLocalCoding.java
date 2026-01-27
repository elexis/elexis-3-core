package ch.elexis.core.findings.model;

import org.apache.commons.lang3.StringUtils;
import java.util.List;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ILocalCoding;
import ch.elexis.core.findings.IObservation.ObservationCode;
import ch.elexis.core.model.IXid;

public class TransientLocalCoding implements ILocalCoding {

	private String system;
	private String code;
	private String display;
	private List<ICoding> mappedCodes;
	private int prio = 0;

	public TransientLocalCoding(String system, String code, String display) {
		this.system = system;
		this.code = code;
		this.display = display;
	}

	public TransientLocalCoding(ObservationCode code) {
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
	public List<ICoding> getMappedCodes() {
		return mappedCodes;
	}

	@Override
	public void setMappedCodes(List<ICoding> mappedCodes) {
		this.mappedCodes = mappedCodes;
	}

	@Override
	public String getId() {
		return null;
	}

	@Override
	public String getLabel() {
		return null;
	}

	@Override
	public boolean addXid(String domain, String id, boolean updateIfExists) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IXid getXid(String domain) {
		return null;
	}

	@Override
	public boolean isDeleted() {
		return false;
	}

	@Override
	public void setDeleted(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public void setDisplay(String display) {
		this.display = display;
	}

	@Override
	public Long getLastupdate() {
		return null;
	}

	@Override
	public void setPrio(int prio) {
		this.prio = prio;
	}

	@Override
	public int getPrio() {
		return prio;
	}
}
