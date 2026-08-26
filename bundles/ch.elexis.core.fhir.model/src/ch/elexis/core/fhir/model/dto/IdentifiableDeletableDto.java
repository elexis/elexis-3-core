package ch.elexis.core.fhir.model.dto;

import java.util.HashMap;
import java.util.Map;

import ch.elexis.core.model.Deleteable;
import ch.elexis.core.model.IXid;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.WithExtInfo;

public class IdentifiableDeletableDto implements Identifiable, Deleteable, WithExtInfo {

	String id;
	String label;
	Long lastupdate;
	boolean deleted;

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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Long getLastupdate() {
		return lastupdate;
	}

	public void setLastupdate(Long lastupdate) {
		this.lastupdate = lastupdate;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public Map<Object, Object> getExtInfo() {
		return extInfo;
	}

	public void setExtInfo(Map<Object, Object> extInfo) {
		this.extInfo = extInfo;
	}

}
