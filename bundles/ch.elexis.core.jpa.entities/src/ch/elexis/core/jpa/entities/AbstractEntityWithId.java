package ch.elexis.core.jpa.entities;

import jakarta.persistence.MappedSuperclass;

/**
 * Abstract class for {@link EntityWithId} instances. This is NOT a JPA
 * {@link MappedSuperclass} as that can cause trouble with weaving. Only
 * implement non JPA logic here.
 *
 * @author thomas
 *
 */
public abstract class AbstractEntityWithId implements EntityWithId {

	@Override
	public int hashCode() {
		return idHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return idEquals(this, obj);
	}

	public int idHashCode(EntityWithId obj) {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((obj.getId() == null) ? 0 : obj.getId().hashCode());
		return result;
	}

	public boolean idEquals(EntityWithId obj, Object other) {
		if (obj == other)
			return true;
		if (other == null)
			return false;
		if (obj.getClass() != other.getClass())
			return false;
		EntityWithId otherEntity = (EntityWithId) other;
		if (obj.getId() == null) {
			if (otherEntity.getId() != null)
				return false;
		} else if (!obj.getId().equals(otherEntity.getId()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getName() + " id=[" + getId() + "]";
	}
}
