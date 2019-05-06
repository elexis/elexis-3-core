package ch.elexis.core.services;

import ch.elexis.core.services.IXidService.IXidDomain;

public class XidDomain implements IXidDomain {
	
	private String domainName;
	private String simpleName;
	private int quality;
	
	private String displayOptions;
	
	public XidDomain(String domainName, String simpleName, int quality, String displayOptions){
		this.domainName = domainName;
		this.simpleName = simpleName;
		this.quality = quality;
		this.displayOptions = displayOptions;
	}
	
	@Override
	public String getDomainName(){
		return domainName;
	}
	
	@Override
	public void setDomainName(String domainName){
		this.domainName = domainName;
	}
	
	@Override
	public String getSimpleName(){
		return simpleName;
	}
	
	@Override
	public void setSimpleName(String simpleName){
		this.simpleName = simpleName;
	}
	
	@Override
	public int getQuality(){
		return quality;
	}
	
	@Override
	public void setQuality(int quality){
		this.quality = quality;
	}
	
	public Object getDisplayOptions(){
		return displayOptions;
	}
}
