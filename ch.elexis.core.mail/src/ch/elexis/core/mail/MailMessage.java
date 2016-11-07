package ch.elexis.core.mail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * Class representing a Message that can be sent using a {@link MailAccount} and a
 * {@link IMailClient}. <br />
 * Example usage:<br />
 * <code>new MailMessage().to("receiver@there.com").subject("subject").text("text");</code>
 * 
 * @author thomas
 *
 */
public class MailMessage {
	
	private String to;
	private String subject;
	private String text;
	
	private List<File> attachments;
	
	/**
	 * Set the to address.
	 * 
	 * @param to
	 * @return
	 */
	public MailMessage to(String to){
		setTo(to);
		return this;
	}
	
	/**
	 * Set the subject.
	 * 
	 * @param subject
	 * @return
	 */
	public MailMessage subject(String subject){
		setSubject(subject);
		return this;
	}
	
	/**
	 * Set the text of the message.
	 * 
	 * @param text
	 * @return
	 */
	public MailMessage text(String text){
		setText(text);
		return this;
	}
	
	/**
	 * Get the to address.
	 * 
	 * @return
	 */
	public String getTo(){
		return to;
	}
	
	/**
	 * Get the to address as {@link InternetAddress}.
	 * 
	 * @return
	 * @throws AddressException
	 */
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
	
	/**
	 * Test if the message has attachments.
	 * 
	 * @return
	 */
	public boolean hasAttachments(){
		return attachments != null && !attachments.isEmpty();
	}
	
	/**
	 * Get all attachments as {@link File} instances.
	 * 
	 * @return
	 */
	public List<File> getAttachments(){
		return attachments;
	}
	
	/**
	 * Add an attachment.
	 * 
	 * @param attachment
	 */
	public void addAttachment(File attachment){
		if (attachments == null) {
			attachments = new ArrayList<File>();
		}
		attachments.add(attachment);
	}
}
