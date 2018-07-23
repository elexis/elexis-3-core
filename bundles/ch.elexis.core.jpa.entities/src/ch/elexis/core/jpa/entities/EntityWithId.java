package ch.elexis.core.jpa.entities;

import java.math.BigInteger;

public interface EntityWithId {
	public String getId();
	
	public void setId(String id);
	
	public BigInteger getLastupdate();
	
	public void setLastupdate(BigInteger lastupdate);
	
}
