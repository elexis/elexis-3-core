package ch.elexis.core.model;

import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.jpa.entities.DbImage;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import jakarta.persistence.Transient;

public class Image extends AbstractIdDeleteModelAdapter<DbImage> implements IdentifiableWithXid, IImage {

	public Image(DbImage entity) {
		super(entity);
	}

	@Override
	public LocalDate getDate() {
		return getEntity().getDate();
	}

	@Override
	public void setDate(LocalDate value) {
		getEntityMarkDirty().setDate(value);
	}

	@Override
	public String getPrefix() {
		return getEntity().getPrefix();
	}

	@Override
	public void setPrefix(String value) {
		getEntityMarkDirty().setPrefix(value);
	}

	@Override
	public String getTitle() {
		return getEntity().getTitle();
	}

	@Override
	public void setTitle(String value) {
		getEntityMarkDirty().setTitle(value);
	}

	@Override
	public byte[] getImage() {
		return getEntity().getImage();
	}

	@Override
	public void setImage(byte[] value) {
		getEntityMarkDirty().setImage(value);
	}

	@Override
	public void setId(String id) {
		getEntityMarkDirty().setId(id);
	}

	@Override
	public MimeType getMimeType() {
		int val = findTitleMimetypeSeparator();
		if (val != -1) {
			return MimeType.valueOf(getTitle().substring(val + 1));
		}
		return null;
	}

	@Override
	public void setMimeType(MimeType value) {
		int val = findTitleMimetypeSeparator();
		String title = getTitle();
		if (val != -1) {
			title = title.substring(val);
		}
		setTitle(title + "." + value.name());
	}

	@Transient
	private int findTitleMimetypeSeparator() {
		String title = getTitle();
		if (StringUtils.isNotBlank(title)) {
			return title.lastIndexOf('.');
		}
		return -1;
	}

}
