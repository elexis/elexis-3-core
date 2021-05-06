package ch.elexis.core.services.eenv;

import java.util.Map;

public class RocketchatMessage {
	
	private String sender;
	private String text;
	private Map<String, Object> attachments;
	
	public String getSender(){
		return sender;
	}
	
	public void setSender(String sender){
		this.sender = sender;
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
