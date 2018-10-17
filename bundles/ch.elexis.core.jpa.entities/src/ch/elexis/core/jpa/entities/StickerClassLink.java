package ch.elexis.core.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "ETIKETTEN_OBJCLASS_LINK")
@EntityListeners(EntityWithIdListener.class)
public class StickerClassLink {
	
	@Column(length = 80)
	private String objclass;
	
	@ManyToOne
	@JoinColumn(name = "sticker", nullable = false, insertable = false)
	private Sticker sticker;
	
	public String getObjclass(){
		return objclass;
	}
	
	public void setObjclass(String objclass){
		this.objclass = objclass;
	}
	
	public Sticker getSticker(){
		return sticker;
	}
	
	public void setSticker(Sticker sticker){
		this.sticker = sticker;
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
	
}
