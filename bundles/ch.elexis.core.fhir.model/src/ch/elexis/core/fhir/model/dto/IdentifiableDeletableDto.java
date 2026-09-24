package ch.elexis.core.fhir.model.dto;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import ch.elexis.core.model.Deleteable;
import ch.elexis.core.model.IXid;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.WithExtInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IdentifiableDeletableDto implements Identifiable, Deleteable, WithExtInfo {

	String id;
	String label;
	Long lastupdate;
	boolean deleted;

	private Map<Object, Object> extInfo = new HashMap<Object, Object>();

	@Override
	public boolean addXid(String domain, String id, boolean updateIfExists) {
		// FIXME
		LoggerFactory.getLogger(getClass()).warn("addXid find solution: " + domain + " " + id);
		return true;
	}

	@Override
	public IXid getXid(String domain) {
		// FIXME
		LoggerFactory.getLogger(getClass()).warn("getXid find solution: " + domain);
		return null;
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

	public Map<Object, Object> getExtInfo() {
		return extInfo;
	}

	public void setExtInfo(Map<Object, Object> extInfo) {
		this.extInfo = extInfo;
	}

}
