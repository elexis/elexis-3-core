package ch.elexis.core.model;

import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.model.mixin.IdentifiableWithXid;
import ch.elexis.core.model.util.ModelUtil;

public class Xid extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Xid>
		implements IdentifiableWithXid, IXid {
	
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
	
	@Override
	public <T> T getObject(Class<T> clazz){
		return ModelUtil.load(getObjectId(), clazz);
	}
	
	@Override
	public void setObject(Object object){
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
