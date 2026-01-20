package ch.elexis.core.fhir.model.dto;

import java.util.HashMap;
import java.util.Map;

import ch.elexis.core.model.Deleteable;
import ch.elexis.core.model.IXid;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.WithExtInfo;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class IdentifiableDeletableDto implements Identifiable, Deleteable, WithExtInfo {

	@Setter(AccessLevel.NONE)
	String id;
	String label;
	Long lastupdate;
	boolean deleted;

	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private Map<Object, Object> extInfo = new HashMap<Object, Object>();

	@Override
	public boolean addXid(String domain, String id, boolean updateIfExists) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IXid getXid(String domain) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getExtInfo(Object key) {
		return extInfo.get(key);
	}

	@Override
	public void setExtInfo(Object key, Object value) {
		extInfo.put(key, value);
	}

	@Override
	public Map<Object, Object> getMap() {
		return extInfo;
	}
}
