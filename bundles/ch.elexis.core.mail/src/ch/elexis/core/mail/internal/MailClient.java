package ch.elexis.core.mail.internal;

import java.io.File;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.angus.mail.imap.IMAPFolder;
import org.eclipse.angus.mail.imap.IMAPMessage;
import org.eclipse.angus.mail.imap.IMAPStore;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.mail.IMAPMailMessage;
import ch.elexis.core.mail.IMailClient;
import ch.elexis.core.mail.MailAccount;
import ch.elexis.core.mail.MailAccount.TYPE;
import ch.elexis.core.mail.MailMessage;
import ch.elexis.core.services.IConfigService;
import jakarta.activation.CommandMap;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.activation.MailcapCommandMap;
import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.Flags;
import jakarta.mail.Flags.Flag;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.search.SearchTerm;

@Component
public class MailClient implements IMailClient {

	private static final Logger logger = LoggerFactory.getLogger(MailClient.class);

	private static final String CONFIG_ACCOUNTS = "ch.elexis.core.mail/accounts";
	private static final String CONFIG_ACCOUNT = "ch.elexis.core.mail/account";

	private static final String ACCOUNTS_SEPARATOR = ",";

	@Reference
	private IConfigService configService;

	private ErrorTyp lastError;

	@Activate
	private void activate() {
		MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
		mc.addMailcap("text/html;; x-java-content-handler=org.eclipse.angus.mail.handlers.text_html");
		mc.addMailcap("text/xml;; x-java-content-handler=org.eclipse.angus.mail.handlers.text_xml");
		mc.addMailcap("text/plain;; x-java-content-handler=org.eclipse.angus.mail.handlers.text_plain");
		mc.addMailcap("multipart/mixed;; x-java-content-handler=org.eclipse.angus.mail.handlers.multipart_mixed");
		mc.addMailcap("message/rfc822;; x-java-content- handler=org.eclipse.angus.mail.handlers.message_rfc822");
		mc.addMailcap(
				"message/disposition-notification;;x-java-content-handler=org.eclipse.angus.mail.handlers.text_plain");
		CommandMap.setDefaultCommandMap(mc);
	}

	@Override
	public Optional<ErrorTyp> getLastError() {
		ErrorTyp ret = lastError;
		lastError = null;
		return Optional.ofNullable(ret);
	}

	@Override
	public Optional<MailAccount> getAccount(String id) {
		MailAccount ret = null;
		String accountString = configService.getLocal(CONFIG_ACCOUNT + "/" + id, null);
		if (accountString == null) {
			accountString = configService.get(CONFIG_ACCOUNT + "/" + id, null);
		}
		if (accountString != null) {
			ret = MailAccount.from(accountString);
		}
		return Optional.ofNullable(ret);
	}

	@Override
	public List<String> getAccounts() {
		List<String> ret = new ArrayList<>();
		String accountIds = configService.get(CONFIG_ACCOUNTS, null);
		if (accountIds != null) {
			String[] currentIds = accountIds.split(ACCOUNTS_SEPARATOR);
			ret.addAll(Arrays.asList(currentIds));
		}
		return ret;
	}

	@Override
	public List<String> getAccountsLocal() {
		List<String> ret = new ArrayList<>();
		String accountIds = configService.getLocal(CONFIG_ACCOUNTS, null);
		if (accountIds != null) {
			String[] currentIds = accountIds.split(ACCOUNTS_SEPARATOR);
			ret.addAll(Arrays.asList(currentIds));
		}
		return ret;
	}

	@Override
	public void saveAccount(MailAccount account) {
		if (account != null && account.getId() != null) {
			addAccountId(account.getId());
			configService.set(CONFIG_ACCOUNT + "/" + account.getId(), account.toString());
		}
	}

	@Override
	public void saveAccountLocal(MailAccount account) {
		if (account != null && account.getId() != null) {
			addAccountIdLocal(account.getId());
			configService.setLocal(CONFIG_ACCOUNT + "/" + account.getId(), account.toString());
		}
	}

	private void addAccountIdLocal(String id) {
		if (id.contains(ACCOUNTS_SEPARATOR)) {
			throw new IllegalStateException("Id can not contain separator [" + ACCOUNTS_SEPARATOR + "]");
		}
		String accountIds = configService.getLocal(CONFIG_ACCOUNTS, null);
		if (accountIds == null) {
			configService.setLocal(CONFIG_ACCOUNTS, id);
		} else {
			String[] currentIds = accountIds.split(ACCOUNTS_SEPARATOR);
			for (String string : currentIds) {
				if (string.equals(id)) {
					return;
				}
			}
			// not already in list
			configService.setLocal(CONFIG_ACCOUNTS, accountIds + ACCOUNTS_SEPARATOR + id);
		}
	}

	private void addAccountId(String id) {
		if (id.contains(ACCOUNTS_SEPARATOR)) {
			throw new IllegalStateException("Id can not contain separator [" + ACCOUNTS_SEPARATOR + "]");
		}
		String accountIds = configService.get(CONFIG_ACCOUNTS, null);
		if (accountIds == null) {
			configService.set(CONFIG_ACCOUNTS, id);
		} else {
			String[] currentIds = accountIds.split(ACCOUNTS_SEPARATOR);
			for (String string : currentIds) {
				if (string.equals(id)) {
					return;
				}
			}
			// not already in list
			configService.set(CONFIG_ACCOUNTS, accountIds + ACCOUNTS_SEPARATOR + id);
		}
	}

	@Override
	public void removeAccount(MailAccount account) {
		if (account != null && account.getId() != null) {
			removeAccountId(account.getId());
			configService.set(CONFIG_ACCOUNT + "/" + account.getId(), null);
		}
	}

	@Override
	public void removeAccountLocal(MailAccount account) {
		if (account != null && account.getId() != null) {
			removeAccountIdLocal(account.getId());
			configService.setLocal(CONFIG_ACCOUNT + "/" + account.getId(), null);
		}
	}

	private void removeAccountIdLocal(String id) {
		String accountIds = configService.getLocal(CONFIG_ACCOUNTS, null);
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
				configService.setLocal(CONFIG_ACCOUNTS, sb.toString());
			} else {
				configService.setLocal(CONFIG_ACCOUNTS, null);
			}
		}
	}

	private void removeAccountId(String id) {
		String accountIds = configService.get(CONFIG_ACCOUNTS, null);
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
				configService.set(CONFIG_ACCOUNTS, sb.toString());
			} else {
				configService.set(CONFIG_ACCOUNTS, null);
			}
		}
	}

	@Override
	public boolean testAccount(MailAccount account) {
		MailClientProperties properties = new MailClientProperties(account);

		try {
			if (account.getType() == TYPE.SMTP) {
				Session session = Session.getInstance(properties.getProperties(), new jakarta.mail.Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(account.getUsername(), account.getPassword());
					}
				});
				Transport transport = session.getTransport();
				transport.connect();
				transport.close();
			} else if (account.getType() == TYPE.IMAP) {
				Session session = Session.getInstance(properties.getProperties(), new jakarta.mail.Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(account.getUsername(), account.getPassword());
					}
				});
				Store store = session.getStore("imap");

				store.connect();
				store.close();
			} else if (account.getType() == TYPE.IMAPS) {
				Session session = Session.getInstance(properties.getProperties(), new jakarta.mail.Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(account.getUsername(), account.getPassword());
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
	public boolean sendMail(MailAccount account, MailMessage message) {
		System.setProperty("mail.mime.encodeparameters", "false");
		MailClientProperties properties = new MailClientProperties(account);

		try {
			if (account.getType() == TYPE.SMTP) {
				Session session = Session.getInstance(properties.getProperties(), new jakarta.mail.Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(account.getUsername(), account.getPassword());
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

				transport.sendMessage(mimeMessage, addressesList.toArray(new InternetAddress[addressesList.size()]));
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
	public List<IMAPMailMessage> getMessages(MailAccount account, String sourceFolder, boolean flag,
			boolean onlyFetchUnflagged) throws MessagingException {
		if (account.getType() == TYPE.SMTP) {
			logger.warn("Invalid account type for receiving [" + account.getType() + "].");
			lastError = ErrorTyp.CONFIGTYP;
			return Collections.emptyList();
		}

		if (sourceFolder == null) {
			sourceFolder = "INBOX";
		}

		IMAPStore imapStore = null;
		try {
			imapStore = (IMAPStore) getSession(account).getStore();
			imapStore.connect();
			List<IMAPMailMessage> listMessages = new ArrayList<>();
			Folder folder = imapStore.getFolder(sourceFolder);

			int openMode = (flag) ? Folder.READ_WRITE : Folder.READ_ONLY;
			folder.open(openMode);

			Message[] messages = folder.getMessages();
			for (Message _message : messages) {
				if (onlyFetchUnflagged && _message.getFlags().contains(Flag.FLAGGED)) {
					continue;
				}
				IMAPMailMessage selfSustainableCopy = IMAPMailMessage.of((IMAPMessage) _message);
				listMessages.add(selfSustainableCopy);
				if (flag) {
					_message.setFlag(Flag.FLAGGED, true);
				}
			}
			folder.close(false);
			return listMessages;
		} finally {
			if (imapStore != null && imapStore.isConnected()) {
				imapStore.close();
			}
		}
	}

	private Session getSession(MailAccount account) {
		MailClientProperties properties = new MailClientProperties(account);
		return Session.getInstance(properties.getProperties(), new jakarta.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(account.getUsername(), account.getPassword());
			}
		});
	}

	@Override
	public void moveMessage(MailAccount account, IMAPMailMessage message, @Nullable String sourceFolder,
			String targetFolder, boolean removeFlag) throws MessagingException {

		if (sourceFolder == null) {
			sourceFolder = "INBOX";
		}

		if (targetFolder == null) {
			throw new MessagingException("targetFolder must not be null");
		}

		IMAPStore imapStore = null;
		try {
			imapStore = (IMAPStore) getSession(account).getStore();
			imapStore.connect();

			IMAPFolder _sourceFolder = (IMAPFolder) imapStore.getFolder(sourceFolder);
			IMAPFolder _targetFolder = (IMAPFolder) imapStore.getFolder(targetFolder);

			// find message in sourcefolder
			MimeMessage referrer = (MimeMessage) message.toIMAPMessage();
			SearchTerm searchTerm = new MySearchTerm(referrer.getSubject(), referrer.getSize());

			_sourceFolder.open(Folder.READ_WRITE);
			Message[] matches = _sourceFolder.search(searchTerm);

			if (matches != null && matches.length == 1) {
				// found the message, lets move it

				_targetFolder.open(Folder.READ_WRITE);

				Message[] _message = new Message[] { matches[0] };

				if (removeFlag) {
					_message[0].setFlag(Flag.FLAGGED, false);
				}

				_sourceFolder.setFlags(_message, new Flags(Flags.Flag.SEEN), true);
				// TODO support direct move operation,
				// requires newer javax.mail https://github.com/javaee/javamail/releases
				// and imapStore.hasCapability("MOVE")
				_sourceFolder.copyMessages(_message, _targetFolder);
				_sourceFolder.setFlags(_message, new Flags(Flags.Flag.DELETED), true);

				_sourceFolder.close(true);
				_targetFolder.close(false);

			} else {
				throw new MessagingException("Could not find message in sourcefolder");
			}

		} finally {
			if (imapStore != null && imapStore.isConnected()) {
				imapStore.close();
			}
		}
	}

	private void handleException(MessagingException e) {
		if (e instanceof AuthenticationFailedException) {
			lastError = ErrorTyp.AUTHENTICATION;
		} else if (e.getNextException() instanceof UnknownHostException
				|| e.getNextException() instanceof ConnectException) {
			lastError = ErrorTyp.CONNECTION;
		} else if (e instanceof AddressException) {
			lastError = ErrorTyp.ADDRESS;
		}
	}

	private class MySearchTerm extends SearchTerm {

		private static final long serialVersionUID = -5686587458681566618L;

		private final String subject;
		private final int size;

		public MySearchTerm(String subject, int size) {
			this.subject = subject;
			this.size = size;
		}

		@Override
		public boolean match(Message msg) {
			try {
				return (Objects.equals(msg.getSubject(), subject) && msg.getSize() == size);
			} catch (MessagingException e) {
				logger.warn("Error matching message", e);
				return false;
			}
		}

	}
}
