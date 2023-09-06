package ch.elexis.core.model;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.model.service.holder.CoreModelServiceHolder;

public class UserConfig extends AbstractIdModelAdapter<ch.elexis.core.jpa.entities.Userconfig>
		implements IdentifiableWithXid, IUserConfig {

	public UserConfig(ch.elexis.core.jpa.entities.Userconfig entity) {
		super(entity);
	}

	@Override
	public String getId() {
		return getEntity().getId();
	}

	@Override
	public String getLabel() {
		return getEntity().getOwnerId() + StringUtils.SPACE + getEntity().getParam() + " -> " + getEntity().getValue();
	}

	@Override
	public String getKey() {
		return getEntity().getParam();
	}

	@Override
	public void setKey(String value) {
		getEntityMarkDirty().setParam(value);
	}

	@Override
	public String getValue() {
		return getEntity().getValue();
	}

	@Override
	public void setValue(String value) {
		getEntityMarkDirty().setValue(value);
	}

	@Override
	public IContact getOwner() {
		if (getEntity().getOwnerId() != null) {
			Optional<IContact> owner = CoreModelServiceHolder.get().load(getEntity().getOwnerId(), IContact.class);
			return (IContact) owner.orElse(null);
		}
		return null;
	}

	@Override
	public void setOwner(IContact value) {
		if (value instanceof AbstractIdDeleteModelAdapter) {
			getEntityMarkDirty().setOwnerId(value.getId());
		} else if (value == null) {
			getEntityMarkDirty().setOwnerId(null);
		}
	}
}
