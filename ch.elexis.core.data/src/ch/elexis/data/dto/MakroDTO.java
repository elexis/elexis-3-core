package ch.elexis.data.dto;

public class MakroDTO {
	private String makroUserId;
	private String makroParam;
	
	private String makroName;
	private String makroContent;
	
	public MakroDTO(String userId, String param, String name, String content){
		this.makroUserId = userId;
		this.makroParam = param;
		this.makroName = name;
		this.makroContent = content;
	}
	
	@Override
	public String toString(){
		return makroName;
	}
	
	public String getMakroName(){
		return makroName;
	}
	
	public void setMakroName(String makroName){
		this.makroName = makroName;
	}
	
	public String getMakroContent(){
		return makroContent;
	}
	
	public void setMakroContent(String makroContent){
		this.makroContent = makroContent;
	}

	public String getMakroUserId(){
		return makroUserId;
	}

	public void setMakroUserId(String makroUserId){
		this.makroUserId = makroUserId;
	}

	public String getMakroParam(){
		return makroParam;
	}

	public void setMakroParam(String makroParam){
		this.makroParam = makroParam;
	}
}
