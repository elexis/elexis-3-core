package ch.elexis.core.mail.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.FileLocator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import com.icegreen.greenmail.junit4.GreenMailRule;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.sun.mail.imap.IMAPStore;

import ch.elexis.core.mail.IMAPMailMessage;
import ch.elexis.core.mail.IMailClient;
import ch.elexis.core.mail.MailAccount;
import ch.elexis.core.mail.MailAccount.TYPE;
import ch.elexis.core.utils.OsgiServiceUtil;
import jakarta.mail.BodyPart;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

public class MailClientImapTest {

	private static IMailClient client;

	@Rule
	public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP);

	private GreenMailUser user;
	private MailAccount account;

	@BeforeClass
	public static void beforeClass() {
		client = OsgiServiceUtil.getService(IMailClient.class).get();
	}

	@Before
	public void before() {
		user = greenMail.setUser("to@localhost", "login-id", "password");

		account = new MailAccount();
		account.setId("testImapAccount");
		account.setType(TYPE.IMAP);
		account.setUsername(user.getLogin());
		account.setPassword(user.getPassword());
		account.setHost("localhost");
		account.setPort(Integer.toString(greenMail.getImap().getPort()));
	}

	@After
	public void after() {
		List<String> accounts = client.getAccounts();
		for (String string : accounts) {
			Optional<MailAccount> account = client.getAccount(string);
			if (account.isPresent()) {
				client.removeAccount(account.get());
			}
		}
		accounts = client.getAccounts();
		assertTrue(accounts.isEmpty());
	}

	public void aassgetMailsInFolder() throws MessagingException, IOException {
		MailAccount account = new MailAccount();
		account.setId("testImapAccount");
		account.setType(TYPE.IMAP);
		account.setUsername("igidev@medevit.at");
		account.setPassword("AL6Gz#5l!f");
		account.setHost("imap.world4you.com");
		account.setPort("143");

		List<IMAPMailMessage> messages = client.getMessages(account, "ElexisInbox", false, false);
		for (IMAPMailMessage message : messages) {

			String subject = message.getSubject();
			String sentDate = message.getSentDate().toString();
			String messageContent = message.getText();
			String attachFiles;

			System.out.println("\t Subject: " + subject);
			System.out.println("\t Sent Date: " + sentDate);
			System.out.println("\t Message: " + messageContent);
		}
	}

	@Test
	public void getMailsInFolderSimpleMail() throws MessagingException, IOException {
		Session smtpSession = greenMail.getSmtp().createSession();
		Message msg = new MimeMessage(smtpSession);
		msg.setFrom(new InternetAddress("foo@example.com"));
		msg.addRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
		msg.setSubject("Email sent to GreenMail via plain JavaMail");
		msg.setText("Fetch me via IMAP");
		Transport.send(msg);

		List<IMAPMailMessage> messages = client.getMessages(account, null, false, false);
		assertEquals(1, messages.size());
		assertEquals("Fetch me via IMAP", messages.get(0).getText());
		assertEquals("foo@example.com", messages.get(0).getSender());
	}

	@Test
	public void getMailsInFolderComplexMail()
			throws AddressException, MessagingException, URISyntaxException, IOException {
		Session smtpSession = greenMail.getSmtp().createSession();
		Message msg = new MimeMessage(smtpSession);
		msg.setFrom(new InternetAddress("Foo Bar <foo@example.com>"));
		msg.addRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
		msg.setSubject("Patient: Testperson Armeswesen (m), 10.05.1990");

		BodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setText("Sehr geehrter Herr Testperson\n" + "\n"
				+ "Wir hoffen, dass Sie bald wieder gesund werden.\n" + "\n" + "Freundliche Grüsse\n" + "\n" + "Herr\n"
				+ "Dr. med. Fabian Müller Fuchs\n" + "Nieder\n" + "5443 Niederrohrdorf\n" + "");

		MimeBodyPart attachmentPart = new MimeBodyPart();
		URL testPdfUrl = getClass().getClassLoader()
				.getResource("/rsc/1_Testperson_Armeswesen_Laborblatt_Mail_17082020_145507.pdf");
		File file = new File(FileLocator.resolve(testPdfUrl).toURI());
		attachmentPart.attachFile(file);

		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(messageBodyPart);
		multipart.addBodyPart(attachmentPart);

		msg.setContent(multipart);

		Transport.send(msg);

		List<IMAPMailMessage> messages = client.getMessages(account, null, false, false);
		assertEquals(1, messages.size());
		assertTrue(messages.get(0).getText().startsWith("Sehr geehrter"));
		assertEquals(1, messages.get(0).getAttachments().size());
		assertEquals("1_Testperson_Armeswesen_Laborblatt_Mail_17082020_145507.pdf",
				messages.get(0).getAttachments().get(0).getFilename());
		assertEquals("foo@example.com", messages.get(0).getSender());
	}

	@Test
	public void moveMailInImap() throws MessagingException {
		final IMAPStore store = greenMail.getImap().createStore();
		store.connect(user.getLogin(), user.getPassword());
		Folder folder = store.getFolder("archive");
		folder.create(Folder.HOLDS_FOLDERS | Folder.HOLDS_MESSAGES);
		store.close();

		Session smtpSession = greenMail.getSmtp().createSession();
		Message msg = new MimeMessage(smtpSession);
		msg.setFrom(new InternetAddress("foo@example.com"));
		msg.addRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
		msg.setSubject("Email sent to GreenMail via plain JavaMail");
		msg.setText("Fetch me via IMAP");
		Transport.send(msg);

		Message msg2 = new MimeMessage(smtpSession);
		msg2.setFrom(new InternetAddress("foobert@example.com"));
		msg2.addRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
		msg2.setSubject("Email sent to GreenMail via plain JavaMail, another one");
		msg2.setText("Fetch me via IMAP - additional blabla");
		Transport.send(msg2);

		List<IMAPMailMessage> messages = client.getMessages(account, null, false, false);
		assertEquals(2, messages.size());
		assertEquals("Fetch me via IMAP", messages.get(0).getText());
		client.moveMessage(account, messages.get(0), null, "archive", true);

		messages = client.getMessages(account, null, false, false);
		assertEquals(1, messages.size());

		messages = client.getMessages(account, "archive", false, false);
		assertEquals(1, messages.size());
		assertEquals("Email sent to GreenMail via plain JavaMail", messages.get(0).getSubject());
	}

}
