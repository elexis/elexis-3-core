package ch.elexis.core.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "ETIKETTEN_OBJECT_LINK")
@EntityListeners(EntityWithIdListener.class)
public class StickerObjectLink {
	
	@Column(length = 80, nullable = false)
	private String obj;
	
	@ManyToOne
	@JoinColumn(name = "etikette")
	private Sticker sticker;
	
	public String getObj(){
		return obj;
	}
	
	public void setObj(String obj){
		this.obj = obj;
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
		result = prime * result + ((obj == null) ? 0 : obj.hashCode());
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
		StickerObjectLink other = (StickerObjectLink) obj;
		if (this.obj == null) {
			if (other.obj != null)
				return false;
		} else if (!this.obj.equals(other.obj))
			return false;
		if (sticker == null) {
			if (other.sticker != null)
				return false;
		} else if (!sticker.equals(other.sticker))
			return false;
		return true;
	}	
	
}
