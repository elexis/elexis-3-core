package ch.elexis.core.mail;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ch.elexis.core.model.IImage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

/**
 * Class representing a Message that can be sent using a {@link MailAccount} and
 * a {@link IMailClient}. <br />
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

	public static MailMessage fromJson(Serializable serializable) {
		return gson.fromJson(gson.toJson(serializable), MailMessage.class);
	}

	public static MailMessage fromMap(@SuppressWarnings("rawtypes") Map map) {
		MailMessage mailMessage = new MailMessage();
		mailMessage.setSubject((String) map.get("subject"));
		mailMessage.setTo((String) map.get("to"));
		mailMessage.setCc((String) map.get("cc"));
		mailMessage.setText((String) map.get("text"));
		mailMessage.setDocuments((String) map.get("documentsString"));
		mailMessage.setAttachments((String) map.get("attachmentsString"));
		return mailMessage;
	}

	private String to;
	private String cc;
	private String subject;
	private String text;

	private String attachmentsString;

	private String documentsString;

	private List<String> imageStrings;

	/**
	 * Set the to address.
	 *
	 * @param to
	 * @return
	 */
	public MailMessage to(String to) {
		setTo(to);
		return this;
	}

	/**
	 * Set the cc address.
	 *
	 * @param to
	 * @return
	 */
	public MailMessage cc(String cc) {
		setCc(cc);
		return this;
	}

	/**
	 * Set the subject.
	 *
	 * @param subject
	 * @return
	 */
	public MailMessage subject(String subject) {
		setSubject(subject);
		return this;
	}

	/**
	 * Set the text of the message.
	 *
	 * @param text
	 * @return
	 */
	public MailMessage text(String text) {
		setText(text);
		return this;
	}

	/**
	 * Get the to address.
	 *
	 * @return
	 */
	public String getTo() {
		return to;
	}

	/**
	 * Get the to address.
	 *
	 * @return
	 */
	public String getCc() {
		return cc;
	}

	/**
	 * Get the to address as {@link InternetAddress}.
	 *
	 * @return
	 * @throws AddressException
	 */
	public InternetAddress[] getToAddress() throws AddressException {
		if (StringUtils.isNotEmpty(getTo())) {
			return InternetAddress.parse(getTo());
		}
		return new InternetAddress[0];
	}

	public InternetAddress[] getCcAddress() throws AddressException {
		if (StringUtils.isNotEmpty(getCc())) {
			return InternetAddress.parse(getCc());
		}
		return new InternetAddress[0];
	}

	public void setTo(String to) {
		this.to = to;
	}

	public void setCc(String cc) {
		this.cc = cc;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getText() {
		return text;
	}

	public String getHtmlText() {
		return text.replace(StringUtils.LF, "<br />\n");
	}

	public void setText(String text) {
		this.text = text;
		parseImage();
	}

	private void parseImage() {
		if (StringUtils.isNotEmpty(text) && text.indexOf("<img src=\"") != -1) {
			imageStrings = Jsoup.parse(text).select("img[src]").stream().map(e -> e.attr("src"))
					.filter(s -> StringUtils.isNotBlank(s)).toList();
			imageStrings.forEach(s -> {
				if (!loadImage(s).isPresent()) {
					LoggerFactory.getLogger(getClass()).warn("Image for [" + s + "] not found");
				}
			});
		}
	}

	/**
	 * Test if the message has attachments.
	 *
	 * @return
	 */
	public boolean hasAttachments() {
		return StringUtils.isNotBlank(documentsString) || StringUtils.isNotBlank(attachmentsString);
	}

	/**
	 * Get all attachments as {@link File} instances.
	 *
	 * @return
	 */
	public List<File> getAttachments() {
		String attachmentFilesString = attachmentsString;
		if (StringUtils.isNotBlank(attachmentFilesString) && !StringUtils.isBlank(documentsString)) {
			attachmentFilesString += ":::" + AttachmentsUtil.toAttachments(documentsString);
		} else if (!StringUtils.isBlank(documentsString)) {
			attachmentFilesString = AttachmentsUtil.toAttachments(documentsString);
		}
		return AttachmentsUtil.getAttachmentsFiles(attachmentFilesString);
	}

	public void setAttachments(String attachments) {
		this.attachmentsString = attachments;
	}

	public String getAttachmentsString() {
		return attachmentsString;
	}

	public void setDocuments(String documents) {
		this.documentsString = documents;
	}

	public String getDocumentsString() {
		return documentsString;
	}

	public boolean hasImage() {
		return imageStrings != null && !imageStrings.isEmpty();
	}

	@SuppressWarnings("unchecked")
	private Optional<IImage> loadImage(String imageString) {
		Optional<IImage> ret = Optional.empty();
		IQuery<IImage> query = CoreModelServiceHolder.get().getQuery(IImage.class);
		query.and("prefix", COMPARATOR.EQUALS, "ch.elexis.core.mail");
		query.and("title", COMPARATOR.LIKE, getImageContentId(imageString) + "%");
		ret = query.executeSingleResult();
		if (ret.isEmpty()) {
			Optional<?> value = ContextServiceHolder.get()
					.getNamed("ch.elexis.core.mail.image." + getImageContentId(imageString));
			if (value.isPresent() && value.get() instanceof IImage) {
				ret = (Optional<IImage>) value;
			}
		}
		return ret;
	}

	public List<String> getImageStrings() {
		return imageStrings;
	}

	public File getImage(String imageString) {
		Optional<IImage> image = loadImage(imageString);
		if (image.isPresent()) {
			return AttachmentsUtil.getAttachmentsFile(image.get());
		}
		return null;
	}

	public String getImageContentId(String imageString) {
		return imageString.substring(imageString.indexOf("cid:") + "cid:".length(), imageString.length());
	}
}
