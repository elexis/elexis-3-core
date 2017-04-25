package ch.elexis.data.dto;

import ch.elexis.core.model.ITag;

public class TagDocumentDTO implements ITag {
	
	private String name;
	
	public TagDocumentDTO(String name){
		super();
		this.name = name;
	}
	
	@Override
	public String getName(){
		return name;
	}
	
	@Override
	public void setName(String value){
		this.name = value;
	}
	
}
