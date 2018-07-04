package ch.elexis.core.jpa.entities;

import java.util.Objects;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AbstractDBObjectId extends AbstractDBObject {

	public abstract String getId();

	public abstract void setId(String id);
	
	public abstract String getLabel();
	
	@Override
	public boolean equals(Object obj){
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractDBObjectId other = (AbstractDBObjectId) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}
	
	@Override
	public int hashCode(){
		return Objects.hash(getClass(), getId());
	}
}
