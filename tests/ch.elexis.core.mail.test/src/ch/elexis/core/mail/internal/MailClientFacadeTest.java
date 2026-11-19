package ch.elexis.core.mail.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.util.List;

import org.eclipse.angus.mail.imap.IMAPStore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.gson.Gson;
import com.icegreen.greenmail.junit4.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetup;

import ch.elexis.core.mail.IMailClient.ErrorTyp;
import ch.elexis.core.mail.MailAccount;
import ch.elexis.core.mail.MailClientConfig;
import ch.elexis.core.mail.MailMessage;
import ch.elexis.core.mail.internal.connection.MailSessionFactory;
import ch.elexis.core.mail.model.MailSecurity;
import ch.elexis.core.mail.model.MailboxAccount;
import ch.elexis.core.mail.model.MailboxScope;
import ch.elexis.core.mail.model.SentCopyMode;
import ch.elexis.core.mail.model.ServerEndpoint;
import jakarta.mail.Folder;
import jakarta.mail.Multipart;

/** Verifies compatibility selection and actual SMTP delivery without an active user. */
public class MailClientFacadeTest extends MailLegacyTestSupport {
	/** Uses an isolated SMTP endpoint for the system send path. */
	@Rule
	public final GreenMailRule greenMail = new GreenMailRule(new ServerSetup[] { new ServerSetup(0, "127.0.0.1", "smtp"),
			new ServerSetup(0, "127.0.0.1", "imap") });

	/** Isolates file attachments from the installation's document store. */
	@Rule
	public final TemporaryFolder temporary = new TemporaryFolder();

	/**
	 * Lists all stored accounts in configuration order, including personal
	 * accounts.
	 */
	@Test
	public void listsAccountsInOrderIncludingPersonal() {
		client.saveAccount(legacy("first", MailAccount.TYPE.SMTP));
		client.saveAccount(legacy("second", MailAccount.TYPE.IMAP));
		MailboxAccount a = accounts.getAccount("first").orElseThrow();
		MailboxAccount personal = new MailboxAccount("personal", a.displayName(), a.emailAddress(), a.provider(),
				MailboxScope.PERSONAL, "owner", List.of(), List.of(), List.of(), a.imap(), a.smtp(), a.username(),
				a.authMode(), false, "", a.sentCopyMode(), 2, 120);
		global.put(MailClientConfig.account("personal"), new Gson().toJson(personal));
		global.put(MailClientConfig.ACCOUNTS, "second,personal,first");
		assertEquals(List.of("second", "personal", "first"), client.getAccounts());
		assertEquals(MailAccount.TYPE.SMTP, client.getAccount("personal").orElseThrow().getType());
	}

	/** Shared views expose sender settings and mandators without exposing a password. */
	@Test
	public void sharedViewDoesNotExposePassword() {
		MailAccount old = legacy("shared", MailAccount.TYPE.SMTP);
		old.setFrom("practice@example.test");
		old.setMandants("m1||m2");
		old.setStarttls(true);
		client.saveAccount(old);
		MailAccount view = client.getAccount("shared").orElseThrow();
		assertNull(view.getPassword());
		assertEquals(old.getHost(), view.getHost());
		assertEquals(old.getPort(), view.getPort());
		assertEquals(old.getUsername(), view.getUsername());
		assertEquals(old.getFrom(), view.getFrom());
		assertTrue(view.isStarttls());
		assertEquals("m1||m2", view.getMandants());
	}

	/** A local legacy override takes precedence and remains in the old station configuration. */
	@Test
	public void localOverrideHasPrecedence() {
		client.saveAccount(legacy("same", MailAccount.TYPE.SMTP));
		MailAccount override = legacy("same", MailAccount.TYPE.IMAP);
		override.setHost("station.example.test");
		client.saveAccountLocal(override);
		assertEquals("station.example.test", client.getAccount("same").orElseThrow().getHost());
		assertEquals(List.of("same"), client.getAccountsLocal());
		client.removeAccountLocal(override);
		assertTrue(client.getAccountsLocal().isEmpty());
		assertEquals("relay.example.test", client.getAccount("same").orElseThrow().getHost());
	}

	/** Removing the last shared account clears its new keys while retaining legacy source keys. */
	@Test
	public void removeClearsNewKeysOnly() {
		MailAccount old = legacy("remove", MailAccount.TYPE.SMTP);
		old.setMandants("mandator");
		global.put("ch.elexis.core.mail/account/remove", old.toString());
		client.saveAccount(old);
		client.removeAccount(old);
		assertTrue(client.getAccounts().isEmpty());
		assertFalse(client.getAccount("remove").isPresent());
		assertNull(global.get(MailClientConfig.legacy("remove")));
		assertNull(global.get(MailClientConfig.mandators("remove")));
		assertNull(global.get(MailClientConfig.password("remove")));
		assertEquals(old.toString(), global.get("ch.elexis.core.mail/account/remove"));
	}

	/** Rejects overlong database keys before writing any account data. */
	@Test
	public void rejectsOverlongConfigurationKeys() {
		assertThrows(IllegalArgumentException.class, () -> client.saveAccount(legacy("x".repeat(60), MailAccount.TYPE.SMTP)));
		assertTrue(global.isEmpty());
	}

	/** Sends through encrypted shared credentials, even though the public view has no password. */
	@Test
	public void sendsSharedAccountWithoutActiveUser() throws Exception {
		MailAccount old = smtp();
		client.saveAccount(old);
		MailAccount view = client.getAccount(old.getId()).orElseThrow();
		assertTrue(client.sendMail(view, new MailMessage().to("recipient@example.test").cc("copy@example.test")
				.subject("Subject").text("Body")));
		assertTrue(greenMail.waitForIncomingEmail(5000, 2));
		assertEquals("Subject", greenMail.getReceivedMessages()[0].getSubject());
		jakarta.mail.Multipart content = (jakarta.mail.Multipart) greenMail.getReceivedMessages()[0].getContent();
		assertTrue(content.getBodyPart(0).getContent().toString().contains("Body"));
	}

	/** Uses the stored station password rather than caller-supplied shared credentials. */
	@Test
	public void sendsLocalLegacyAccount() {
		MailAccount old = smtp();
		client.saveAccountLocal(old);
		MailAccount supplied = legacy(old.getId(), MailAccount.TYPE.SMTP);
		supplied.setPassword("wrong");
		assertTrue(client.sendMail(supplied, new MailMessage().to("recipient@example.test").subject("Local").text("Body")));
	}

	/** Preserves legacy authentication errors and the read-and-clear error accessor. */
	@Test
	public void mapsAuthenticationError() {
		MailAccount old = smtp();
		old.setPassword("wrong");
		assertFalse(client.testAccount(old));
		assertEquals(ErrorTyp.AUTHENTICATION, client.getLastError().orElseThrow());
		assertFalse(client.getLastError().isPresent());
	}

	/** Keeps attachment names and bytes in the legacy related MIME structure. */
	@Test
	public void sendsLegacyFileAttachment() throws Exception {
		java.io.File file = temporary.newFile("report.txt");
		Files.writeString(file.toPath(), "Attachment content");
		MailMessage message = new MailMessage().to("recipient@example.test").subject("File").text("Body");
		message.setAttachments(file.getAbsolutePath());
		assertTrue(client.sendMail(smtp(), message));
		assertTrue(greenMail.waitForIncomingEmail(5000, 1));
		Multipart content = (Multipart) greenMail.getReceivedMessages()[0].getContent();
		assertEquals(2, content.getCount());
		assertEquals("report.txt", content.getBodyPart(1).getFileName());
		assertEquals("Attachment content", content.getBodyPart(1).getContent());
	}

	/** Appends the sent copy and reads IMAP through a shared account whose legacy view is SMTP. */
	@Test
	public void dualProtocolAccountKeepsSentCopyAndDetachedRead() throws Exception {
		MailAccount old = smtp();
		client.saveAccount(old);
		MailboxAccount a = accounts.getAccount(old.getId()).orElseThrow();
		MailboxAccount dual = new MailboxAccount(a.id(), a.displayName(), a.emailAddress(), a.provider(), a.scope(),
				a.ownerUserId(), a.delegateUserIds(), a.visibleUserIds(), a.visibleGroupIds(),
				new ServerEndpoint("127.0.0.1", greenMail.getImap().getPort(), MailSecurity.NONE), a.smtp(),
				a.username(), a.authMode(), false, "", SentCopyMode.CLIENT, 2, 120);
		global.put(MailClientConfig.account(a.id()), new Gson().toJson(dual));
		try (IMAPStore connection = new MailSessionFactory().createStore(dual)) {
			connection.connect(dual.imap().host(), dual.imap().port(), old.getUsername(), old.getPassword());
			connection.getFolder("Sent").create(Folder.HOLDS_MESSAGES);
		}
		MailAccount view = client.getAccount(a.id()).orElseThrow();
		assertEquals(MailAccount.TYPE.SMTP, view.getType());
		assertTrue(client.sendMail(view, new MailMessage().to("recipient@example.test").subject("Sent copy").text("Body")));
		List<ch.elexis.core.mail.IMAPMailMessage> messages = client.getMessages(view, "Sent", true, true);
		assertEquals(1, messages.size());
		assertEquals("Sent copy", messages.getFirst().getSubject());
		assertTrue(client.getMessages(view, "Sent", false, true).isEmpty());
		assertEquals("sender@example.test", messages.getFirst().getSender());
	}

	/** SERVER mode leaves the IMAP sent folder untouched after SMTP delivery. */
	@Test
	public void serverCopyModeDoesNotAppend() throws Exception {
		MailAccount old = smtp();
		client.saveAccount(old);
		MailboxAccount a = accounts.getAccount(old.getId()).orElseThrow();
		MailboxAccount dual = new MailboxAccount(a.id(), a.displayName(), a.emailAddress(), a.provider(), a.scope(),
				a.ownerUserId(), a.delegateUserIds(), a.visibleUserIds(), a.visibleGroupIds(),
				new ServerEndpoint("127.0.0.1", greenMail.getImap().getPort(), MailSecurity.NONE), a.smtp(),
				a.username(), a.authMode(), false, "", SentCopyMode.SERVER, 2, 120);
		global.put(MailClientConfig.account(a.id()), new Gson().toJson(dual));
		try (IMAPStore connection = new MailSessionFactory().createStore(dual)) {
			connection.connect(dual.imap().host(), dual.imap().port(), old.getUsername(), old.getPassword());
			connection.getFolder("Sent").create(Folder.HOLDS_MESSAGES);
		}
		assertTrue(client.sendMail(client.getAccount(a.id()).orElseThrow(),
				new MailMessage().to("recipient@example.test").subject("Server copy").text("Body")));
		assertTrue(greenMail.waitForIncomingEmail(5000, 1));
		assertEquals(1, greenMail.getReceivedMessages().length);
		assertTrue(client.getMessages(client.getAccount(a.id()).orElseThrow(), "Sent", false, false).isEmpty());
	}

	private MailAccount smtp() {
		greenMail.setUser("sender@example.test", "sender@example.test", "secret-with-padding");
		greenMail.setUser("recipient@example.test", "recipient@example.test", "secret");
		greenMail.setUser("copy@example.test", "copy@example.test", "secret");
		MailAccount old = legacy("smtp", MailAccount.TYPE.SMTP);
		old.setHost("127.0.0.1");
		old.setPort(Integer.toString(greenMail.getSmtp().getPort()));
		return old;
	}
}
