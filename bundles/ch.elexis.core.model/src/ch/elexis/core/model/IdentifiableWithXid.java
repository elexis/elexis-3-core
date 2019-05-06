package ch.elexis.core.model;

import ch.elexis.core.services.holder.XidServiceHolder;

public interface IdentifiableWithXid extends Identifiable {
	
	@Override
	public default boolean addXid(String domain, String id, boolean updateIfExists){
		return XidServiceHolder.get().addXid(this, domain, id, updateIfExists);
	}
	
	@Override
	public default IXid getXid(String domain){
		return XidServiceHolder.get().getXid(this, domain);
	}
}
