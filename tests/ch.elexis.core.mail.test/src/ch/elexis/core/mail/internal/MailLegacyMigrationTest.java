package ch.elexis.core.mail.internal;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Base64;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import ch.elexis.core.mail.MailAccount;
import ch.elexis.core.mail.MailClientConfig;
import ch.elexis.core.mail.internal.account.CredentialCipher;
import ch.elexis.core.mail.internal.legacy.MailLegacyAccountConverter;
import ch.elexis.core.mail.internal.legacy.MailLegacyMigration;
import ch.elexis.core.mail.model.AuthMode;
import ch.elexis.core.mail.model.MailSecurity;
import ch.elexis.core.mail.model.MailboxAccount;
import ch.elexis.core.mail.model.MailboxScope;

/** Regression coverage for protocol conversion, secrets and the installation migration claim. */
public class MailLegacyMigrationTest extends MailLegacyTestSupport {

	/** Retains one SMTP endpoint, legacy security, mandators and encrypted credentials. */
	@Test
	public void migratesSmtpWithoutDeletingLegacyKeys() throws Exception {
		MailAccount old = legacy("relay", MailAccount.TYPE.SMTP);
		old.setFrom("practice@example.test");
		old.setMandants("m1||m2");
		String encoded = old.toString();
		global.put("ch.elexis.core.mail/accounts", old.getId());
		global.put("ch.elexis.core.mail/account/relay", encoded);
		migrate(migration());
		MailboxAccount saved = accounts.getAccount("relay").orElseThrow();
		assertEquals("relay", saved.displayName());
		assertEquals("practice@example.test", saved.emailAddress());
		assertEquals(MailboxScope.SHARED, saved.scope());
		assertEquals(AuthMode.PASSWORD, saved.authMode());
		assertNull(saved.imap());
		assertEquals(MailSecurity.NONE, saved.smtp().security());
		assertTrue(saved.visibleUserIds().isEmpty());
		assertTrue(saved.visibleGroupIds().isEmpty());
		assertTrue(accounts.isLegacy("relay"));
		assertEquals(List.of("m1", "m2"), accounts.getMandatorIds("relay"));
		String secret = global.get(MailClientConfig.password("relay"));
		assertTrue(secret.startsWith("v1:"));
		assertNotEquals(old.getPassword(), secret);
		CredentialCipher cipher = new CredentialCipher(Base64.getDecoder().decode(global.get(MailClientConfig.SECRET_SALT)));
		assertArrayEquals(old.getPassword().toCharArray(), cipher.decrypt(secret));
		assertEquals(encoded, global.get("ch.elexis.core.mail/account/relay"));
		assertEquals("relay", global.get("ch.elexis.core.mail/accounts"));
		assertEquals("done", global.get("mail/client/legacy/migration"));
	}

	/** Keeps IMAPS, SMTP 465 and STARTTLS conversion identical to the old import form. */
	@Test
	public void mapsLegacySecurityAndDefaults() {
		MailAccount old = legacy("imap", MailAccount.TYPE.IMAPS);
		old.setPort("993");
		MailboxAccount converted = MailLegacyAccountConverter.toMailbox(old);
		assertNull(converted.smtp());
		assertEquals(MailSecurity.SSL, converted.imap().security());
		assertEquals(old.getUsername(), converted.emailAddress());
		assertEquals(2, converted.connectionBudget());
		assertEquals(120, converted.pollIntervalSeconds());
		old.setType(MailAccount.TYPE.SMTP);
		old.setPort("465");
		assertEquals(MailSecurity.SSL, MailLegacyAccountConverter.toMailbox(old).smtp().security());
		old.setStarttls(true);
		assertEquals(MailSecurity.STARTTLS, MailLegacyAccountConverter.toMailbox(old).smtp().security());
	}

	/** Retains passwords whose old Base64 representation contains padding. */
	@Test
	public void readsPaddedLegacyPassword() {
		MailAccount old = legacy("padded", MailAccount.TYPE.SMTP);
		old.setPassword("x");
		assertEquals("x", MailAccount.from(old.toString()).getPassword());
	}

	/** Skips new definitions and never imports station-local account overrides. */
	@Test
	public void skipsExistingAndLocalAccounts() throws Exception {
		MailAccount old = legacy("existing", MailAccount.TYPE.SMTP);
		client.saveAccount(old);
		String before = global.get(MailClientConfig.account(old.getId()));
		String secret = global.get(MailClientConfig.password(old.getId()));
		global.put("ch.elexis.core.mail/accounts", "existing");
		global.put("ch.elexis.core.mail/account/existing", legacy("existing", MailAccount.TYPE.IMAP).toString());
		client.saveAccountLocal(legacy("local", MailAccount.TYPE.SMTP));
		migrate(migration());
		assertEquals(before, global.get(MailClientConfig.account("existing")));
		assertEquals(secret, global.get(MailClientConfig.password("existing")));
		assertFalse(accounts.getAccount("local").isPresent());
		assertTrue(client.getAccount("local").isPresent());
	}

	/** A malformed legacy account does not prevent later accounts from being migrated and keeps the migration open. */
	@Test
	public void continuesAfterMalformedAccount() throws Exception {
		global.put("ch.elexis.core.mail/accounts", "broken,valid");
		global.put("ch.elexis.core.mail/account/broken", "invalid");
		global.put("ch.elexis.core.mail/account/valid", legacy("valid", MailAccount.TYPE.IMAP).toString());
		migrate(migration());
		assertFalse(accounts.getAccount("broken").isPresent());
		assertTrue(accounts.getAccount("valid").isPresent());
		assertNull(global.get("mail/client/legacy/migration"));
		String valid = global.get(MailClientConfig.account("valid"));
		global.put("ch.elexis.core.mail/account/broken", legacy("broken", MailAccount.TYPE.SMTP).toString());
		migrate(migration());
		assertTrue(accounts.getAccount("broken").isPresent());
		assertEquals(valid, global.get(MailClientConfig.account("valid")));
		assertEquals("done", global.get("mail/client/legacy/migration"));
	}

	/** Does not restart a completed or interrupted installation migration. */
	@Test
	public void existingMarkerPreventsMigration() throws Exception {
		for (String state : List.of("running", "done")) {
			global.put("mail/client/legacy/migration", state);
			migrate(migration());
			assertEquals(0, claims.get());
			assertEquals(state, global.get("mail/client/legacy/migration"));
		}
	}

	/** Two station starts racing after their initial read produce exactly one insert winner. */
	@Test
	public void concurrentStartsHaveOneWinner() throws Exception {
		global.put("ch.elexis.core.mail/accounts", "shared");
		global.put("ch.elexis.core.mail/account/shared", legacy("shared", MailAccount.TYPE.SMTP).toString());
		claimBarrier = new CyclicBarrier(2);
		MailLegacyMigration first = migration();
		MailLegacyMigration second = migration();
		ExecutorService workers = Executors.newFixedThreadPool(2);
		try {
			Future<?> one = workers.submit(() -> { migrate(first); return null; });
			Future<?> two = workers.submit(() -> { migrate(second); return null; });
			one.get(10, TimeUnit.SECONDS);
			two.get(10, TimeUnit.SECONDS);
			assertEquals(1, claims.get());
			assertEquals(1, migratedWrites.get());
			assertEquals("done", global.get("mail/client/legacy/migration"));
		} finally {
			workers.shutdownNow();
		}
	}
}
