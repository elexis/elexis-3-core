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
@Table(name = "ETIKETTEN_OBJECT_LINK")
@EntityListeners(EntityWithIdListener.class)
@IdClass(StickerObjectLinkId.class)
@Cache(expiry = 15000)
@NamedQuery(name = "StickerObjectLink.obj", query = "SELECT st FROM StickerObjectLink st WHERE st.obj = :obj")
@NamedQuery(name = "StickerObjectLink.etikette", query = "SELECT st FROM StickerObjectLink st WHERE st.etikette = :etikette")
@NamedQuery(name = "StickerObjectLink.obj.etikette", query = "SELECT st FROM StickerObjectLink st WHERE st.obj = :obj AND st.etikette = :etikette")
public class StickerObjectLink implements EntityWithId {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@Column(length = 25, nullable = false)
	private String obj;

	@Id
	@Column(length = 25, nullable = false)
	private String etikette;

	@Column(length = 256, nullable = true)
	private String data;

	public String getObj() {
		return obj;
	}

	public void setObj(String obj) {
		this.obj = obj;
	}

	public String getEtikette() {
		return etikette;
	}

	public void setEtikette(String stickerId) {
		this.etikette = stickerId;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((obj == null) ? 0 : obj.hashCode());
		result = prime * result + ((etikette == null) ? 0 : etikette.hashCode());
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
		StickerObjectLink other = (StickerObjectLink) obj;
		if (this.obj == null) {
			if (other.obj != null)
				return false;
		} else if (!this.obj.equals(other.obj))
			return false;
		if (etikette == null) {
			if (other.etikette != null)
				return false;
		} else if (!etikette.equals(other.etikette))
			return false;
		return true;
	}

	@Override
	public String getId() {
		return getObj() + "_" + getEtikette();
	}

	@Override
	public void setId(String id) {
		setObj(id);
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
