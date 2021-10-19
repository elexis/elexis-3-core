package ch.elexis.core.logback.rocketchat.internal;

import java.util.Map;

public class RocketchatLogMessage {
	
	private String username;
	private String text;
	private Map<String, Object> attachments;
	

	public String getUsername(){
		return username;
	}
	
	public void setUsername(String username){
		this.username = username;
	}
	
	
	public String getText(){
		return text;
	}
	
	public void setText(String text){
		this.text = text;
	}
	
	public Map<String, Object> getAttachments(){
		return attachments;
	}
	
	public void setAttachments(Map<String, Object> attachments){
		this.attachments = attachments;
	}
	
}
