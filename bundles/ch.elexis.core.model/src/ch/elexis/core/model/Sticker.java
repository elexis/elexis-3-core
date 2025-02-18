package ch.elexis.core.model;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.jpa.entities.DbImage;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.model.util.internal.ModelUtil;
import jakarta.persistence.Transient;

public class Sticker extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Sticker>
		implements IdentifiableWithXid, ISticker {

	@Transient
	private Identifiable attachedToIdentifiable;

	@Transient
	private String attachedToData;

	public Sticker(ch.elexis.core.jpa.entities.Sticker entity) {
		super(entity);
	}

	@Override
	public void setId(String id) {
		getEntityMarkDirty().setId(id);
	}

	@Override
	public String getBackground() {
		return StringUtils.defaultString(getEntity().getBackground(), "ffffff");
	}

	@Override
	public void setBackground(String value) {
		getEntityMarkDirty().setBackground(value);
	}

	@Override
	public String getForeground() {
		return StringUtils.defaultString(getEntity().getForeground(), "000000");
	}

	@Override
	public void setForeground(String value) {
		getEntityMarkDirty().setForeground(value);
	}

	@Override
	public boolean isVisible() {
		return getImportance() >= 0;
	}

	@Override
	public void setVisible(boolean value) {
		int importance = getImportance();
		if (isVisible()) {
			if (!value) {
				if (importance == 0) {
					setImportance(-1);
				} else {
					setImportance((importance * -1));
				}
			}
		} else {
			if (value) {
				if (importance == -1) {
					setImportance(0);
				} else {
					setImportance((importance * -1));
				}
			}
		}
	}

	@Override
	public int compareTo(ISticker o) {
		if (o != null) {
			return o.getImportance() - getImportance();
		}
		return 1;
	}

	@Override
	public String getName() {
		return getEntity().getName();
	}

	@Override
	public void setName(String value) {
		getEntityMarkDirty().setName(value);
	}

	@Override
	public int getImportance() {
		return getEntity().getImportance();
	}

	@Override
	public void setImportance(int value) {
		getEntityMarkDirty().setImportance(value);
	}

	@Override
	public IImage getImage() {
		return ModelUtil.getAdapter(getEntity().getImage(), IImage.class);
	}

	@Override
	public void setImage(IImage value) {
		if (value != null) {
			getEntityMarkDirty().setImage(((AbstractIdModelAdapter<DbImage>) value).getEntity());
		} else {
			getEntityMarkDirty().setImage(null);
		}
	}

	@Override
	public String getLabel() {
		return StringUtils.defaultString(getEntity().getName());
	}

	@Override
	public Identifiable getAttachedTo() {
		return attachedToIdentifiable;
	}

	@Override
	public void setAttachedTo(Identifiable identifiable) {
		this.attachedToIdentifiable = identifiable;
	}

	@Override
	public String getAttachedToData() {
		return this.attachedToData;
	}

	@Override
	public void setAttachedToData(String attachedToData) {
		this.attachedToData = attachedToData;
	}
}
