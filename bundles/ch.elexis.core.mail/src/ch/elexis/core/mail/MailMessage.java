package ch.elexis.core.mail;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;


/**
 * Class representing a Message that can be sent using a {@link MailAccount} and a
 * {@link IMailClient}. <br />
 * Example usage:<br />
 * <code>new MailMessage().to("receiver@there.com").subject("subject").text("text");</code>
 * 
 * @author thomas
 *
 */
public class MailMessage implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5874662524515670629L;
	
	private String to;
	private String cc;
	private String subject;
	private String text;
	
	private String attachmentsString;
	
	private String documentsString;
	
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
	 * Set the cc address.
	 * 
	 * @param to
	 * @return
	 */
	public MailMessage cc(String cc){
		setCc(cc);
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
	 * Get the to address.
	 * 
	 * @return
	 */
	public String getCc(){
		return cc;
	}
	
	/**
	 * Get the to address as {@link InternetAddress}.
	 * 
	 * @return
	 * @throws AddressException
	 */
	public InternetAddress[] getToAddress() throws AddressException{
		if (StringUtils.isNotEmpty(getTo())) {
			return InternetAddress.parse(getTo());
		}
		return new InternetAddress[0];
	}
	
	public InternetAddress[] getCcAddress() throws AddressException{
		if (StringUtils.isNotEmpty(getCc())) {
			return InternetAddress.parse(getCc());
		}
		return new InternetAddress[0];
	}
	
	public void setTo(String to){
		this.to = to;
	}
	
	public void setCc(String cc){
		this.cc = cc;
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
		return StringUtils.isNotBlank(documentsString) || StringUtils.isNotBlank(attachmentsString);
	}
	
	/**
	 * Get all attachments as {@link File} instances.
	 * 
	 * @return
	 */
	public List<File> getAttachments(){
		String attachmentFilesString = attachmentsString;
		if (StringUtils.isNotBlank(attachmentFilesString)
			&& !StringUtils.isBlank(documentsString)) {
			attachmentFilesString += ":::" + AttachmentsUtil.toAttachments(documentsString);
		} else if (!StringUtils.isBlank(documentsString)) {
			attachmentFilesString = AttachmentsUtil.toAttachments(documentsString);
		}
		return AttachmentsUtil.getAttachmentsFiles(attachmentFilesString);
	}
	
	public void setAttachments(String attachments){
		this.attachmentsString = attachments;
	}
	
	public void setDocuments(String documents){
		this.documentsString = documents;
	}
}
