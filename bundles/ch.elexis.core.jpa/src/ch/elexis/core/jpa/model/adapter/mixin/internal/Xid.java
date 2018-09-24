package ch.elexis.core.jpa.model.adapter.mixin.internal;

import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.mixin.IdentifiableWithXid;
import ch.elexis.core.model.IXid;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.XidQuality;

public class Xid extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Xid>
		implements IdentifiableWithXid, IXid {
	
	private Object object;
	
	public Xid(ch.elexis.core.jpa.entities.Xid entity){
		super(entity);
	}
	
	@Override
	public String getDomain(){
		return getEntity().getDomain();
	}
	
	@Override
	public void setDomain(String value){
		getEntity().setDomain(value);
	}
	
	@Override
	public String getDomainId(){
		return getEntity().getDomainId();
	}
	
	@Override
	public void setDomainId(String value){
		getEntity().setDomainId(value);
	}
	
	@Override
	public String getObjectId(){
		return getEntity().getObject();
	}
	
	@Override
	public void setObjectId(String value){
		getEntity().setObject(value);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObject(Class<T> clazz){
		return (T) object;
	}
	
	@Override
	public void setObject(Object object){
		this.object = object;
		if (object instanceof Identifiable) {
			setObjectId(((Identifiable) object).getId());
		} else {
			throw new IllegalStateException("Object must be an Identifiable");
		}
	}
	
	@Override
	public XidQuality getQuality(){
		return getEntity().getQuality();
	}
	
	@Override
	public void setQuality(XidQuality value){
		getEntity().setQuality(value);
	}
}
