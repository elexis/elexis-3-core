package ch.elexis.core.mail.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;

import ch.elexis.core.mail.IMailClient;
import ch.elexis.core.mail.MailAccount;
import ch.elexis.core.mail.MailAccount.TYPE;
import ch.elexis.core.mail.MailMessage;
import ch.elexis.core.utils.OsgiServiceUtil;
import jakarta.mail.MessagingException;

public class MailClientTest {

	private static IMailClient client;

	private static SimpleSmtpServer server;

	@BeforeClass
	public static void beforeClass() throws InterruptedException {
		client = OsgiServiceUtil.getService(IMailClient.class).get();
		server = SimpleSmtpServer.start(10025);
	}

	@AfterClass
	public static void afterClass() {
		server.stop();
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

	@Test
	public void saveAccount() {
		MailAccount account = new MailAccount();
		account.setId("testAccount");
		account.setType(TYPE.SMTP);
		account.setUsername("testUser");
		account.setPassword("testPassword");
		client.saveAccount(account);
	}

	@Test
	public void getAccount() {
		MailAccount account = new MailAccount();
		assertNotNull(account);
		account.setId("testAccount");
		account.setType(TYPE.SMTP);
		account.setUsername("testUser");
		account.setPassword("testPassword");
		client.saveAccount(account);

		Optional<MailAccount> accountLoaded = client.getAccount("testAccount");
		assertTrue(accountLoaded.isPresent());
		assertEquals("testAccount", accountLoaded.get().getId());
	}

	@Test
	public void getAccounts() {
		MailAccount account = new MailAccount();
		assertNotNull(account);
		account.setId("testSmtpAccount");
		account.setType(TYPE.SMTP);
		account.setUsername("testUser");
		account.setPassword("testPassword");
		client.saveAccount(account);

		List<String> loaded = client.getAccounts();
		assertNotNull(loaded);
		assertFalse(loaded.isEmpty());
		assertEquals(1, loaded.size());

		account = new MailAccount();
		assertNotNull(account);
		account.setId("testImapAccount");
		account.setType(TYPE.IMAP);
		account.setUsername("testUser");
		account.setPassword("testPassword");
		client.saveAccount(account);

		loaded = client.getAccounts();
		assertNotNull(loaded);
		assertFalse(loaded.isEmpty());
		assertEquals(2, loaded.size());
	}

	@Test
	public void testAccount() throws MessagingException {
		MailAccount account = new MailAccount();
		account.setId("testSmtpAccount");
		account.setType(TYPE.SMTP);
		account.setUsername("testUser");
		account.setPassword("testPassword");
		account.setHost("localhost");
		account.setPort("10025");

		assertTrue(client.testAccount(account));
	}

	@Test
	public void sendMail() throws MessagingException {
		MailAccount account = new MailAccount();
		account.setId("testSmtpAccount");
		account.setType(TYPE.SMTP);
		account.setUsername("sender@here.com");
		account.setPassword("testPassword");
		account.setHost("localhost");
		account.setPort("10025");

		MailMessage message = new MailMessage().to("receiver@there.com").subject("subject").text("text");
		assertTrue(client.sendMail(account, message));

		assertTrue(server.getReceivedEmailSize() == 1);
		Iterator<?> emailIter = server.getReceivedEmail();
		SmtpMessage email = (SmtpMessage) emailIter.next();
		assertTrue(email.getHeaderValue("Subject").equals("subject"));
		assertTrue(email.getBody().contains("text"));
	}
}
