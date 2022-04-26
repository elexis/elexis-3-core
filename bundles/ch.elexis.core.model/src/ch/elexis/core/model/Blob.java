package ch.elexis.core.model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

import org.slf4j.LoggerFactory;

import ch.elexis.core.jpa.entities.Heap;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.util.JpaModelUtil;

public class Blob extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Heap>
		implements IdentifiableWithXid, Deleteable, IBlob {

	public Blob(Heap entity) {
		super(entity);
	}

	@Override
	public byte[] getContent() {
		return getEntity().getInhalt();
	}

	@Override
	public void setContent(byte[] value) {
		getEntityMarkDirty().setInhalt(value);
	}

	@Override
	public String getStringContent() {
		byte[] comp = getContent();
		if ((comp == null) || (comp.length == 0)) {
			return "";
		}
		byte[] exp = JpaModelUtil.getExpanded(comp);
		try {
			return new String(exp, "utf-8");
		} catch (Exception ex) {
			LoggerFactory.getLogger(getClass()).error("Error getting String content", ex);
			// should really not happen
			return null;
		}
	}

	@Override
	public void setStringContent(String value) {
		byte[] comp = JpaModelUtil.getCompressed(value);
		setContent(comp);
	}

	@Override
	public LocalDate getDate() {
		return getEntity().getDatum();
	}

	@Override
	public void setDate(LocalDate value) {
		getEntityMarkDirty().setDatum(value);
	}

	@Override
	public void setId(String id) {
		getEntityMarkDirty().setId(id);
	}

	@Override
	public Map<Object, Object> getMapContent() {
		byte[] comp = getContent();
		if ((comp == null) || (comp.length == 0)) {
			return Collections.emptyMap();
		}
		return JpaModelUtil.extInfoFromBytes(comp);
	}

	@Override
	public void setMapContent(Map<Object, Object> map) {
		setContent(JpaModelUtil.extInfoToBytes(map));
	}
}
