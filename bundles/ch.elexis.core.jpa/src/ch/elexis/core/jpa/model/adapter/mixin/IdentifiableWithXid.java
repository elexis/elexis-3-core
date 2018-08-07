package ch.elexis.core.jpa.model.adapter.mixin;

import ch.elexis.core.jpa.entities.Xid;
import ch.elexis.core.jpa.model.util.JpaModelUtil;
import ch.elexis.core.model.IXid;
import ch.elexis.core.model.Identifiable;

public interface IdentifiableWithXid extends Identifiable {
	
	public default boolean addXid(String domain, String id, boolean updateIfExists){
		return JpaModelUtil.addXid(this, domain, id, updateIfExists);
	}
	
	public default IXid getXid(String domain){
		Xid xid = JpaModelUtil.getXid(this, domain).orElse(null);
		if (xid != null) {
			IXid iXid = new ch.elexis.core.jpa.model.adapter.mixin.internal.Xid(xid);
			iXid.setObject(this);
			return iXid;
		}
		return null;
	}
}
