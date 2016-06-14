package ch.elexis.core.mail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class MailMessage {
	
	private String to;
	private String subject;
	private String text;
	
	private List<File> attachments;
	
	public MailMessage to(String to){
		setTo(to);
		return this;
	}
	
	public MailMessage subject(String subject){
		setSubject(subject);
		return this;
	}
	
	public MailMessage text(String text){
		setText(text);
		return this;
	}
	
	public String getTo(){
		return to;
	}
	
	public InternetAddress[] getToAddress() throws AddressException{
		return InternetAddress.parse(getTo());
	}
	
	public void setTo(String to){
		this.to = to;
	}
	
	public String getSubject(){
		return subject;
	}
	
	public void setSubject(String subject){
		this.subject = subject;
	}
	
	public String getText(){
		return text;
	}
	
	public void setText(String text){
		this.text = text;
	}
	
	public boolean hasAttachments(){
		return attachments != null && !attachments.isEmpty();
	}
	
	public List<File> getAttachments(){
		return attachments;
	}
	
	public void addAttachment(File attachment){
		if (attachments == null) {
			attachments = new ArrayList<File>();
		}
		attachments.add(attachment);
	}
}
