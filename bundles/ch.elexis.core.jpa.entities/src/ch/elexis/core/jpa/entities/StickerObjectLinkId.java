package ch.elexis.core.jpa.entities;

import java.io.Serializable;
import java.util.Objects;

public class StickerObjectLinkId implements Serializable {

	private static final long serialVersionUID = -654453520781303717L;

	private String obj;
	private String etikette;

	public StickerObjectLinkId() {
	}

	public StickerObjectLinkId(final String obj, final String etikette) {
		this.obj = obj;
		this.etikette = etikette;
	}

	public String getObj() {
		return obj;
	}

	public void setObj(String obj) {
		this.obj = obj;
	}

	public String getEtikette() {
		return etikette;
	}

	public void setEtikette(String etikette) {
		this.etikette = etikette;
	}

	@Override
	public int hashCode() {
		return Objects.hash(etikette, obj);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StickerObjectLinkId other = (StickerObjectLinkId) obj;
		return Objects.equals(etikette, other.etikette) && Objects.equals(this.obj, other.obj);
	}

}
