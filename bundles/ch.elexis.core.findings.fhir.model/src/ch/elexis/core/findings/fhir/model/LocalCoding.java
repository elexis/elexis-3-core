package ch.elexis.core.findings.fhir.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ILocalCoding;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.util.model.TransientCoding;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.model.IXid;

public class LocalCoding extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.LocalCoding>
		implements ILocalCoding {

	private static String MAPPED_SEPARATOR = "||";
	private static String MAPPED_SEPARATOR_SPLITTER = "\\|\\|";
	private static String MAPPED_FIELD_SEPARATOR = "^";

	public LocalCoding(ch.elexis.core.jpa.entities.LocalCoding entity) {
		super(entity);
	}

	@Override
	public String getSystem() {
		return CodingSystem.ELEXIS_LOCAL_CODESYSTEM.getSystem();
	}

	@Override
	public List<ICoding> getMappedCodes() {
		String mappedString = getEntity().getMapped();
		if (mappedString != null && !mappedString.isEmpty()) {
			return getMappedCodingFromString(mappedString);
		}
		return Collections.emptyList();
	}

	private List<ICoding> getMappedCodingFromString(String encoded) {
		String[] codeStrings = encoded.split(MAPPED_SEPARATOR_SPLITTER);
		if (codeStrings != null && codeStrings.length > 0) {
			List<ICoding> ret = new ArrayList<>();
			for (String string : codeStrings) {
				getCodingFromString(string).ifPresent(c -> ret.add(c));
			}
			return ret;
		}
		return Collections.emptyList();
	}

	@Override
	public void setMappedCodes(List<ICoding> mappedCodes) {
		String encoded = StringUtils.EMPTY;
		if (mappedCodes != null && !mappedCodes.isEmpty()) {
			encoded = getMappedCodingAsString(mappedCodes);
		}
		getEntity().setMapped(encoded);
	}

	private String getMappedCodingAsString(List<ICoding> mappedCoding) {
		StringBuilder sb = new StringBuilder();
		for (ICoding iCoding : mappedCoding) {
			if (sb.length() > 0) {
				sb.append(MAPPED_SEPARATOR);
			}
			sb.append(getAsString(iCoding));
		}
		return sb.toString();
	}

	private String getAsString(ICoding coding) {
		return coding.getSystem() + MAPPED_FIELD_SEPARATOR + coding.getCode() + MAPPED_FIELD_SEPARATOR
				+ coding.getDisplay();
	}

	private Optional<ICoding> getCodingFromString(String encoded) {
		String[] codingParts = encoded.split("\\" + MAPPED_FIELD_SEPARATOR);
		if (codingParts != null && codingParts.length > 1) {
			if (codingParts.length == 2) {
				return Optional.of(new TransientCoding(codingParts[0], codingParts[1], StringUtils.EMPTY));
			} else if (codingParts.length == 3) {
				return Optional.of(new TransientCoding(codingParts[0], codingParts[1], codingParts[2]));
			}
		}
		return Optional.empty();
	}

	@Override
	public void setCode(String code) {
		getEntity().setCode(code);
	}

	@Override
	public void setDisplay(String display) {
		getEntity().setDisplay(display);
	}

	@Override
	public String getCode() {
		return getEntity().getCode();
	}

	@Override
	public String getDisplay() {
		return getEntity().getDisplay();
	}

	@Override
	public boolean addXid(String domain, String id, boolean updateIfExists) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IXid getXid(String domain) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPrio(int prio) {
		getEntity().setPrio(prio);
	}

	@Override
	public int getPrio() {
		return getEntity().getPrio();
	}
}
