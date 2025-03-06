package ch.elexis.core.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import ch.elexis.core.jpa.entities.OutputLogEntity;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;


public class OutputLog extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.OutputLogEntity>
		implements IOutputLog {

	public OutputLog(OutputLogEntity entity) {
		super(entity);
	}

	@Override
	public String getObjectType() {
		return getEntity().getObjectType();
	}

	@Override
	public void setObjectType(String objectType) {
		getEntityMarkDirty().setObjectType(objectType);
	}

	@Override
	public String getObjectId() {
		return getEntity().getObjectId();
	}

	@Override
	public void setObjectId(String objectId) {
		getEntityMarkDirty().setObjectId(objectId);
	}

	@Override
	public String getCreatorId() {
		return getEntity().getCreatorId();
	}

	@Override
	public void setCreatorId(String creatorId) {
		getEntityMarkDirty().setCreatorId(creatorId);
	}

	@Override
	public String getOutputter() {
		return getEntity().getOutputter();
	}

	@Override
	public void setOutputter(String outputter) {
		getEntityMarkDirty().setOutputter(outputter);
	}


	@Override
	public String getOutputterStatus() {
		return getEntity().getOutputterStatus();
	}

	@Override
	public void setOutputterStatus(String outputterStatus) {
		getEntityMarkDirty().setOutputterStatus(outputterStatus);
	}

	@Override
	public String getLabel() {
		String d = getEntity().getDate() != null
				? getEntity().getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
				: "";
		String o = getEntity().getOutputter() != null ? getEntity().getOutputter() : "";
		return d + ":" + o;
	}

	@Override
	public boolean addXid(String domain, String id, boolean updateIfExists) {
		return false;
	}

	@Override
	public IXid getXid(String domain) {
		return null;
	}

	@Override
	public Object getExtInfo(Object key) {
		return extInfoHandler.getExtInfo(key);
	}

	@Override
	public void setExtInfo(Object key, Object value) {
		extInfoHandler.setExtInfo(key, value);
	}

	@Override
	public Map<Object, Object> getMap() {
		return extInfoHandler.getMap();
	}

	@Override
	public LocalDate getDate() {

		return getEntity().getDate();
	}

	@Override
	public void setDate(LocalDate value) {
		getEntityMarkDirty().setDate(value);
	}

}
