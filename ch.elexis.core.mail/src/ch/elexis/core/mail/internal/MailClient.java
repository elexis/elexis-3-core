package ch.elexis.core.mail.internal;

import java.io.File;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.mail.IMailClient;
import ch.elexis.core.mail.MailAccount;
import ch.elexis.core.mail.MailAccount.TYPE;
import ch.elexis.core.mail.MailMessage;

@Component
public class MailClient implements IMailClient {
	
	private static final Logger logger = LoggerFactory.getLogger(MailClient.class);
	
	private static final String CONFIG_ACCOUNTS = "ch.elexis.core.mail/accounts";
	private static final String CONFIG_ACCOUNT = "ch.elexis.core.mail/account";
	
	private static final String ACCOUNTS_SEPARATOR = ",";
	
	private ErrorTyp lastError;
	
	@Override
	public Optional<ErrorTyp> getLastError(){
		ErrorTyp ret = lastError;
		lastError = null;
		return Optional.ofNullable(ret);
	}
	
	@Override
	public Optional<String> getDefaultAccount(){
		// TODO Auto-generated method stub
		return Optional.ofNullable(null);
	}
	
	@Override
	public void setDefaultAccount(String id){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Optional<MailAccount> getAccount(String id){
		MailAccount ret = null;
		String accountString = CoreHub.globalCfg.get(CONFIG_ACCOUNT + "/" + id, null);
		if (accountString != null) {
			ret = MailAccount.from(accountString);
		}
		return Optional.ofNullable(ret);
	}
	
	@Override
	public List<String> getAccounts(){
		List<String> ret = new ArrayList<String>();
		String accountIds = CoreHub.globalCfg.get(CONFIG_ACCOUNTS, null);
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
			CoreHub.globalCfg.set(CONFIG_ACCOUNT + "/" + account.getId(), account.toString());
			CoreHub.globalCfg.flush();
		}
	}
	
	private void addAccountId(String id){
		if (id.contains(ACCOUNTS_SEPARATOR)) {
			throw new IllegalStateException(
				"Id can not contain separator [" + ACCOUNTS_SEPARATOR + "]");
		}
		String accountIds = CoreHub.globalCfg.get(CONFIG_ACCOUNTS, null);
		if (accountIds == null) {
			CoreHub.globalCfg.set(CONFIG_ACCOUNTS, id);
		} else {
			String[] currentIds = accountIds.split(ACCOUNTS_SEPARATOR);
			for (String string : currentIds) {
				if (string.equals(id)) {
					return;
				}
			}
			// not already in list
			CoreHub.globalCfg.set(CONFIG_ACCOUNTS, accountIds + ACCOUNTS_SEPARATOR + id);
			CoreHub.globalCfg.flush();
		}
	}
	
	@Override
	public void removeAccount(MailAccount account){
		if (account != null && account.getId() != null) {
			removeAccountId(account.getId());
			CoreHub.globalCfg.remove(CONFIG_ACCOUNT + "/" + account.getId());
			CoreHub.globalCfg.flush();
		}
	}
	
	private void removeAccountId(String id){
		String accountIds = CoreHub.globalCfg.get(CONFIG_ACCOUNTS, null);
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
			CoreHub.globalCfg.set(CONFIG_ACCOUNTS, sb.toString());
			CoreHub.globalCfg.flush();
		}
	}
	
	@Override
	public boolean testAccount(MailAccount account){
		MailClientProperties properties = new MailClientProperties(account);
		
		try {
			if (account.getType() == TYPE.SMTP) {
				Session session =
					Session.getInstance(properties.getProperties(), new javax.mail.Authenticator() {
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
				messageBodyPart.setText(message.getText());
				multipart.addBodyPart(messageBodyPart);
				
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
				transport.sendMessage(mimeMessage, message.getToAddress());
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
	
	private void handleException(MessagingException e){
		if (e instanceof AuthenticationFailedException) {
			lastError = ErrorTyp.AUTHENTICATION;
		} else if (e.getNextException() instanceof UnknownHostException) {
			lastError = ErrorTyp.CONNECTION;
		} else if (e instanceof AddressException) {
			lastError = ErrorTyp.ADDRESS;
		}
	}
}
