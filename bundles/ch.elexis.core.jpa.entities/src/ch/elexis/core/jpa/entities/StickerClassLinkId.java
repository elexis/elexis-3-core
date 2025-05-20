package ch.elexis.core.jpa.entities;

import java.io.Serializable;
import java.util.Objects;

public class StickerClassLinkId implements Serializable {

	private static final long serialVersionUID = -654453520781303717L;

	private String objclass;
	private String sticker;

	public StickerClassLinkId() {
	}

	public StickerClassLinkId(final String objclass, final String sticker) {
		this.objclass = objclass;
		this.sticker = sticker;
	}

	public String getObjclass() {
		return objclass;
	}

	public void setObjclass(String objclass) {
		this.objclass = objclass;
	}

	public String getSticker() {
		return sticker;
	}

	public void setSticker(String sticker) {
		this.sticker = sticker;
	}

	@Override
	public int hashCode() {
		return Objects.hash(objclass, sticker);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StickerClassLinkId other = (StickerClassLinkId) obj;
		return Objects.equals(objclass, other.objclass) && Objects.equals(sticker, other.sticker);
	}

}
