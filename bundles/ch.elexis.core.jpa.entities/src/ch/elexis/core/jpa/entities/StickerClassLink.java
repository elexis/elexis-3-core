package ch.elexis.core.jpa.entities;

import org.eclipse.persistence.annotations.Cache;

import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(name = "ETIKETTEN_OBJCLASS_LINK")
@EntityListeners(EntityWithIdListener.class)
@IdClass(StickerClassLinkId.class)
@Cache(expiry = 15000)
@NamedQuery(name = "StickerClassLink.sticker", query = "SELECT st FROM StickerClassLink st WHERE st.sticker = :sticker")
@NamedQuery(name = "StickerClassLink.objclass", query = "SELECT st FROM StickerClassLink st WHERE st.objclass = :objclass")
public class StickerClassLink implements EntityWithId {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@Column(length = 80, nullable = false)
	private String objclass;

	@Id
	@Column(length = 25, nullable = false)
	private String sticker;

	public String getObjclass() {
		return objclass;
	}

	public void setObjclass(String objclass) {
		this.objclass = objclass;
	}

	public String getSticker() {
		return sticker;
	}

	public void setSticker(String stickerId) {
		this.sticker = stickerId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((objclass == null) ? 0 : objclass.hashCode());
		result = prime * result + ((sticker == null) ? 0 : sticker.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StickerClassLink other = (StickerClassLink) obj;
		if (objclass == null) {
			if (other.objclass != null)
				return false;
		} else if (!objclass.equals(other.objclass))
			return false;
		if (sticker == null) {
			if (other.sticker != null)
				return false;
		} else if (!sticker.equals(other.sticker))
			return false;
		return true;
	}

	@Override
	public String getId() {
		return getObjclass() + "_" + getSticker();
	}

	@Override
	public void setId(String id) {
		setObjclass(id);
	}

	@Override
	public Long getLastupdate() {
		return lastupdate;
	}

	@Override
	public void setLastupdate(Long lastupdate) {
		this.lastupdate = lastupdate;
	}
}
