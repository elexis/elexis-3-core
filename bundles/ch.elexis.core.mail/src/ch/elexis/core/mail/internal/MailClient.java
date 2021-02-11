package ch.elexis.core.mail.internal;

import java.io.File;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.AuthenticationFailedException;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;

import ch.elexis.core.mail.IMAPMailMessage;
import ch.elexis.core.mail.IMailClient;
import ch.elexis.core.mail.MailAccount;
import ch.elexis.core.mail.MailAccount.TYPE;
import ch.elexis.core.mail.MailMessage;
import ch.elexis.core.services.holder.ConfigServiceHolder;

@Component
public class MailClient implements IMailClient {
	
	private static final Logger logger = LoggerFactory.getLogger(MailClient.class);
	
	private static final String CONFIG_ACCOUNTS = "ch.elexis.core.mail/accounts";
	private static final String CONFIG_ACCOUNT = "ch.elexis.core.mail/account";
	
	private static final String ACCOUNTS_SEPARATOR = ",";
	
	private ErrorTyp lastError;
	
	private IMAPStore imapStore;
	
	@Activate
	private void activate(){
		MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
		mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
		mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
		mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
		mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
		mc.addMailcap(
			"message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822");
	}
	
	@Override
	public Optional<ErrorTyp> getLastError(){
		ErrorTyp ret = lastError;
		lastError = null;
		return Optional.ofNullable(ret);
	}
	
	@Override
	public Optional<MailAccount> getAccount(String id){
		MailAccount ret = null;
		String accountString = ConfigServiceHolder.get().get(CONFIG_ACCOUNT + "/" + id, null);
		if (accountString != null) {
			ret = MailAccount.from(accountString);
		}
		return Optional.ofNullable(ret);
	}
	
	@Override
	public List<String> getAccounts(){
		List<String> ret = new ArrayList<String>();
		String accountIds = ConfigServiceHolder.get().get(CONFIG_ACCOUNTS, null);
		if (accountIds != null) {
			String[] currentIds = accountIds.split(ACCOUNTS_SEPARATOR);
			ret.addAll(Arrays.asList(currentIds));
		}
		return ret;
	}
	
	@Override
	public void saveAccount(MailAccount account){
		if (account != null && account.getId() != null) {
			addAccountId(account.getId());
			ConfigServiceHolder.get().set(CONFIG_ACCOUNT + "/" + account.getId(),
				account.toString());
		}
	}
	
	private void addAccountId(String id){
		if (id.contains(ACCOUNTS_SEPARATOR)) {
			throw new IllegalStateException(
				"Id can not contain separator [" + ACCOUNTS_SEPARATOR + "]");
		}
		String accountIds = ConfigServiceHolder.get().get(CONFIG_ACCOUNTS, null);
		if (accountIds == null) {
			ConfigServiceHolder.get().set(CONFIG_ACCOUNTS, id);
		} else {
			String[] currentIds = accountIds.split(ACCOUNTS_SEPARATOR);
			for (String string : currentIds) {
				if (string.equals(id)) {
					return;
				}
			}
			// not already in list
			ConfigServiceHolder.get().set(CONFIG_ACCOUNTS, accountIds + ACCOUNTS_SEPARATOR + id);
		}
	}
	
	@Override
	public void removeAccount(MailAccount account){
		if (account != null && account.getId() != null) {
			removeAccountId(account.getId());
			ConfigServiceHolder.get().set(CONFIG_ACCOUNT + "/" + account.getId(), null);
		}
	}
	
	private void removeAccountId(String id){
		String accountIds = ConfigServiceHolder.get().get(CONFIG_ACCOUNTS, null);
		if (accountIds != null) {
			StringBuilder sb = new StringBuilder();
			String[] currentIds = accountIds.split(ACCOUNTS_SEPARATOR);
			for (String string : currentIds) {
				if (!string.equals(id)) {
					if (sb.length() > 0) {
						sb.append(ACCOUNTS_SEPARATOR);
					}
					sb.append(string);
				}
			}
			// write new list
			if (StringUtils.isNotBlank(sb.toString())) {
				ConfigServiceHolder.get().set(CONFIG_ACCOUNTS, sb.toString());
			} else {
				ConfigServiceHolder.get().set(CONFIG_ACCOUNTS, null);
			}
		}
	}
	
	@Override
	public boolean testAccount(MailAccount account){
		MailClientProperties properties = new MailClientProperties(account);
		
		try {
			if (account.getType() == TYPE.SMTP) {
				Session session =
					Session.getInstance(properties.getProperties(), new javax.mail.Authenticator() {
						@Override
						protected PasswordAuthentication getPasswordAuthentication(){
							return new PasswordAuthentication(account.getUsername(),
								account.getPassword());
						}
					});
				Transport transport = session.getTransport();
				transport.connect();
				transport.close();
			} else if (account.getType() == TYPE.IMAP) {
				Session session =
					Session.getInstance(properties.getProperties(), new javax.mail.Authenticator() {
						@Override
						protected PasswordAuthentication getPasswordAuthentication(){
							return new PasswordAuthentication(account.getUsername(),
								account.getPassword());
						}
					});
				Store store = session.getStore("imap");
				
				store.connect();
				store.close();
			} else if (account.getType() == TYPE.IMAPS) {
				Session session =
						Session.getInstance(properties.getProperties(), new javax.mail.Authenticator() {
							@Override
							protected PasswordAuthentication getPasswordAuthentication(){
								return new PasswordAuthentication(account.getUsername(),
									account.getPassword());
							}
						});
					Store store = session.getStore("imaps");
					
					store.connect();
					store.close();
			} else {
				logger.warn("Unknown account type [" + account.getType() + "].");
				lastError = ErrorTyp.CONFIGTYP;
				return false;
			}
		} catch (MessagingException e) {
			logger.warn("Error testing account [" + account.getId() + "]", e);
			lastError = ErrorTyp.CONNECTION;
			handleException(e);
			return false;
		}
		return true;
	}
	
	@Override
	public boolean sendMail(MailAccount account, MailMessage message){
		MailClientProperties properties = new MailClientProperties(account);
		
		try {
			if (account.getType() == TYPE.SMTP) {
				Session session =
					Session.getInstance(properties.getProperties(), new javax.mail.Authenticator() {
						@Override
						protected PasswordAuthentication getPasswordAuthentication(){
							return new PasswordAuthentication(account.getUsername(),
								account.getPassword());
						}
					});
				
				MimeMessage mimeMessage = new MimeMessage(session);
				mimeMessage.addHeader("X-ElexisMail", "ch.elexis.core.mail");
				// mail.user attribute of the properties 
				mimeMessage.setFrom(account.getFromAddress());
				mimeMessage.setSubject(message.getSubject());
				
				Multipart multipart = new MimeMultipart();
				// create the message part 
				MimeBodyPart messageBodyPart = new MimeBodyPart();
				messageBodyPart.setText(message.getHtmlText(), "UTF-8", "html");
				multipart.addBodyPart(messageBodyPart);
				
				if (message.hasImage()) {
					File image = message.getImage();
					if (image != null) {
						messageBodyPart = new MimeBodyPart();
						DataSource source = new FileDataSource(image);
						messageBodyPart.setDataHandler(new DataHandler(source));
						messageBodyPart.setHeader("Content-ID", message.getImageContentId());
						multipart.addBodyPart(messageBodyPart);
					}
				}
				
				if (message.hasAttachments()) {
					List<File> attachments = message.getAttachments();
					for (File file : attachments) {
						messageBodyPart = new MimeBodyPart();
						DataSource source = new FileDataSource(file);
						messageBodyPart.setDataHandler(new DataHandler(source));
						messageBodyPart.setFileName(file.getName());
						multipart.addBodyPart(messageBodyPart);
					}
				}
				// Put parts in message
				mimeMessage.setContent(multipart);
				
				Transport transport = session.getTransport();
				transport.connect();
				// add recipients
				List<InternetAddress> addressesList = new ArrayList<>();
				mimeMessage.setRecipients(RecipientType.TO, message.getToAddress());
				addressesList.addAll(Arrays.asList(message.getToAddress()));
				mimeMessage.setRecipients(RecipientType.CC, message.getCcAddress());
				addressesList.addAll(Arrays.asList(message.getCcAddress()));
				
				transport.sendMessage(mimeMessage,
					addressesList.toArray(new InternetAddress[addressesList.size()]));
				transport.close();
			} else {
				logger.warn("Invalid account type for sending [" + account.getType() + "].");
				lastError = ErrorTyp.CONFIGTYP;
				return false;
			}
		} catch (MessagingException e) {
			logger.warn("Error sending using account [" + account.getId() + "]", e);
			handleException(e);
			return false;
		}
		return true;
	}
	
	@Override
	public List<IMAPMailMessage> getMessages(MailAccount account, String path)
		throws MessagingException{
		if (account.getType() == TYPE.SMTP) {
			logger.warn("Invalid account type for receiving [" + account.getType() + "].");
			lastError = ErrorTyp.CONFIGTYP;
			return Collections.emptyList();
		}
		
		if (path == null) {
			path = "INBOX";
		}
		
		MailClientProperties properties = new MailClientProperties(account);
		Session session =
			Session.getInstance(properties.getProperties(), new javax.mail.Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication(){
					return new PasswordAuthentication(account.getUsername(), account.getPassword());
				}
			});
		imapStore = (IMAPStore) session.getStore();
		imapStore.connect();
		List<IMAPMailMessage> listMessages = new ArrayList<IMAPMailMessage>();
		Folder folder = imapStore.getFolder(path);
		folder.open(Folder.READ_ONLY);
		Message[] messages = folder.getMessages();
		for (Message _message : messages) {
			IMAPMailMessage of = IMAPMailMessage.of((IMAPMessage) _message);
			listMessages.add(of);
		}
		return listMessages;
		
	}
	
	@Override
	public void moveMessage(IMAPMailMessage message, String targetFolder) throws MessagingException{
		if (imapStore == null || !imapStore.isConnected()) {
			throw new MessagingException("store is null or not connected");
		}
		Folder sourceFolder = message.toIMAPMessage().getFolder();
		IMAPFolder _targetFolder = (IMAPFolder) imapStore.getFolder(targetFolder);
		try {
			if (sourceFolder.isOpen()) {
				sourceFolder.close(false);
			}
			if (_targetFolder.isOpen()) {
				_targetFolder.close(false);
			}
			sourceFolder.open(Folder.READ_WRITE);
			_targetFolder.open(Folder.READ_WRITE);
			monitorMove(_targetFolder);
			Message[] messages = new Message[] {
				message.toIMAPMessage()
			};
			sourceFolder.setFlags(messages, new Flags(Flags.Flag.SEEN), true);
			sourceFolder.copyMessages(messages, _targetFolder);
			waitForMoveCompletion(_targetFolder, 2000);
			sourceFolder.setFlags(messages, new Flags(Flags.Flag.DELETED), true);
		} finally {
			sourceFolder.close(true);
			if (_targetFolder.isOpen()) {
				_targetFolder.close(false);
			}
		}
	}
	
	private void monitorMove(Folder folder){
		folder.addMessageCountListener(new MessageCountListener() {
			@Override
			public void messagesAdded(MessageCountEvent e){
				synchronized (folder) {
					folder.notify();
				}
			}
			
			@Override
			public void messagesRemoved(MessageCountEvent arg0){}
		});
	}
	
	private void waitForMoveCompletion(Folder folder, int waitTimeout){
		synchronized (folder) {
			try {
				folder.wait(waitTimeout);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void closeStore(MailAccount account){
		if (imapStore != null && imapStore.isConnected()) {
			try {
				imapStore.close();
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		imapStore = null;
	}
	
	private void handleException(MessagingException e){
		if (e instanceof AuthenticationFailedException) {
			lastError = ErrorTyp.AUTHENTICATION;
		} else if (e.getNextException() instanceof UnknownHostException
			|| e.getNextException() instanceof ConnectException) {
			lastError = ErrorTyp.CONNECTION;
		} else if (e instanceof AddressException) {
			lastError = ErrorTyp.ADDRESS;
		}
	}
}
