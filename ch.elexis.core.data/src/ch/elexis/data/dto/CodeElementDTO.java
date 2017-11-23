package ch.elexis.data.dto;

import ch.elexis.core.model.ICodeElement;

public class CodeElementDTO implements ICodeElement {
	
	private String codeSystemName;
	private String id;
	private String code;
	private String text;
	
	public CodeElementDTO(ICodeElement codeElement){
		this.id = codeElement.getId();
		this.code = codeElement.getCode();
		this.codeSystemName = codeElement.getCodeSystemName();
		this.text = codeElement.getText();
	}
	
	public CodeElementDTO(String system, String code){
		this.codeSystemName = system;
		this.code = code;
	}
	
	@Override
	public String getCodeSystemName(){
		return codeSystemName;
	}
	
	public void setCodeSystemName(String codeSystemName){
		this.codeSystemName = codeSystemName;
	}
	
	@Override
	public String getId(){
		return id;
	}
	
	@Override
	public String getCode(){
		return code;
	}
	
	public void setCode(String code){
		this.code = code;
	}
	
	@Override
	public String getText(){
		return text;
	}
	
	public void setText(String text){
		this.text = text;
	}
	
	@Override
	public String toString(){
		return this.codeSystemName + " (" + this.code + ") " + (text != null ? text : "");
	}
}
