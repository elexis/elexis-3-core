package ch.elexis.core.mail;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ch.elexis.core.model.IImage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

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
	
	private static Gson gson = new Gson();
	
	public static MailMessage fromJson(Serializable serializable){
		return gson.fromJson(serializable.toString(), MailMessage.class);
	}
	
	private String to;
	private String cc;
	private String subject;
	private String text;
	
	private String attachmentsString;
	
	private String documentsString;
	
	private String imageString;
	
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
	
	public String getHtmlText(){
		return text.replace("\n", "<br />\n");
	}
	
	public void setText(String text){
		this.text = text;
		parseImage();
	}
	
	private void parseImage(){
		if (StringUtils.isNotEmpty(text) && text.indexOf("<img src=\"") != -1) {
			StringBuilder sb = new StringBuilder();
			char[] characters = text.substring(text.indexOf("<img src=\"")).toCharArray();
			for (char c : characters) {
				sb.append(c);
				if (c == '>') {
					break;
				}
			}
			if (sb.toString().endsWith(">")) {
				imageString = sb.toString();
				if (!loadImage().isPresent()) {
					LoggerFactory.getLogger(getClass())
						.warn("Image for [" + imageString + "] not found");
				}
			}
		}
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
	
	public String getAttachmentsString(){
		return attachmentsString;
	}
	
	public void setDocuments(String documents){
		this.documentsString = documents;
	}
	
	public String getDocumentsString(){
		return documentsString;
	}
	
	public boolean hasImage(){
		return StringUtils.isNotBlank(imageString) && loadImage().isPresent();
	}
	
	private Optional<IImage> loadImage(){
		IQuery<IImage> query = CoreModelServiceHolder.get().getQuery(IImage.class);
		query.and("prefix", COMPARATOR.EQUALS, "ch.elexis.core.mail");
		query.and("title", COMPARATOR.LIKE, getImageContentId() + "%");
		return query.executeSingleResult();
	}
	
	public File getImage(){
		Optional<IImage> image = loadImage();
		if (image.isPresent()) {
			return AttachmentsUtil.getAttachmentsFile(image.get());
		}
		return null;
	}
	
	public String getImageContentId(){
		return imageString.substring(imageString.indexOf("cid:") + "cid:".length(),
			imageString.length() - 2);
	}
}
