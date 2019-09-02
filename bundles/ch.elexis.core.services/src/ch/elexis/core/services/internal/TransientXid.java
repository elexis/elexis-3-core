package ch.elexis.core.services.internal;

import java.util.Date;

import ch.elexis.core.model.IXid;
import ch.elexis.core.model.XidQuality;

public class TransientXid implements IXid {
	
	private String domain;
	private String domainId;
	
	private XidQuality quality;
	
	private Object object;
	
	private Long lastupdate;
	
	public TransientXid(String domain, String domainId, XidQuality quality, Object object){
		this.domain = domain;
		this.domainId = domainId;
		this.quality = quality;
		this.object = object;
		this.lastupdate = new Date().getTime();
	}
	
	@Override
	public boolean isDeleted(){
		return false;
	}
	
	@Override
	public void setDeleted(boolean value){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String getId(){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String getLabel(){
		return toString();
	}
	
	@Override
	public boolean addXid(String domain, String id, boolean updateIfExists){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public IXid getXid(String domain){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Long getLastupdate(){
		return lastupdate;
	}
	
	@Override
	public String getDomain(){
		return domain;
	}
	
	@Override
	public void setDomain(String value){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String getDomainId(){
		return domainId;
	}
	
	@Override
	public void setDomainId(String value){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public XidQuality getQuality(){
		return quality;
	}
	
	@Override
	public void setQuality(XidQuality value){
		throw new UnsupportedOperationException();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObject(Class<T> clazz){
		return (T) object;
	}
	
	@Override
	public void setObject(Object object){
		throw new UnsupportedOperationException();
	}
}
