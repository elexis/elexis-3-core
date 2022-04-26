package ch.elexis.core.model;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.model.util.internal.ModelUtil;

public class Stock extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Stock>
		implements IdentifiableWithXid, IStock {

	public Stock(ch.elexis.core.jpa.entities.Stock entity) {
		super(entity);
	}

	@Override
	public String getCode() {
		return getEntity().getCode();
	}

	@Override
	public String getDriverUuid() {
		return getEntity().getDriverUuid();
	}

	@Override
	public String getDriverConfig() {
		return getEntity().getDriverConfig();
	}

	@Override
	public int getPriority() {
		return getEntity().getPriority();
	}

	@Override
	public IMandator getOwner() {
		if (getEntity().getOwner() != null) {
			return ModelUtil.getAdapter(getEntity().getOwner(), IMandator.class);
		}
		return null;
	}

	@Override
	public void setCode(String value) {
		getEntityMarkDirty().setCode(value);
	}

	@Override
	public void setDriverUuid(String value) {
		getEntityMarkDirty().setDriverUuid(value);
	}

	@Override
	public void setDriverConfig(String value) {
		getEntityMarkDirty().setDriverConfig(value);
	}

	@Override
	public void setPriority(int value) {
		getEntityMarkDirty().setPriority(value);
	}

	@Override
	public void setOwner(IMandator value) {
		if (value != null) {
			if (value instanceof AbstractIdModelAdapter) {
				getEntityMarkDirty().setOwner((Kontakt) ((AbstractIdModelAdapter<?>) value).getEntity());
			}
		} else {
			getEntityMarkDirty().setOwner(null);
		}
	}

	@Override
	public String getLabel() {
		return StringUtils.isNotBlank(getEntity().getDescription())
				? "[" + getCode() + "] " + getEntity().getDescription()
				: getCode();
	}
}
