package ch.elexis.core.jpa.entities;

public interface EntityWithDeleted {
	public boolean isDeleted();
	
	public void setDeleted(boolean deleted);
}
