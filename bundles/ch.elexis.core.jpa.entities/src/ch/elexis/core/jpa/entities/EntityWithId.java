package ch.elexis.core.jpa.entities;

public interface EntityWithId {
	public String getId();
	
	public void setId(String id);
	
	public Long getLastupdate();
	
	public void setLastupdate(Long lastupdate);
	
	public static int idHashCode(EntityWithId obj){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((obj.getId() == null) ? 0 : obj.getId().hashCode());
		return result;
	}
	
	public static boolean idEquals(EntityWithId obj, Object other){
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
}
