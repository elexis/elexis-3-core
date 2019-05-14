package ch.elexis.core.jpa.entities;

import java.io.Serializable;

public class StickerObjectLinkId implements Serializable {
	
	private static final long serialVersionUID = -654453520781303717L;
	
	private String obj;
	private String etikette;
	
	public StickerObjectLinkId(){}
	
	public StickerObjectLinkId(final String obj, final String etikette){
		this.obj = obj;
		this.etikette = etikette;
	}
	
	public String getObj(){
		return obj;
	}
	
	public void setObj(String obj){
		this.obj = obj;
	}
	
	public String getEtikette(){
		return etikette;
	}
	
	public void setEtikette(String etikette){
		this.etikette = etikette;
	}
}
