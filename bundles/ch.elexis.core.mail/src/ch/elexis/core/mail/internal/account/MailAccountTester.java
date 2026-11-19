package ch.elexis.core.mail.internal.account;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;

import javax.net.ssl.SSLException;

import org.eclipse.angus.mail.imap.IMAPStore;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.mail.MailClientConfig;
import ch.elexis.core.mail.internal.connection.MailSessionFactory;
import ch.elexis.core.mail.internal.oauth.OAuth2TokenCache;
import ch.elexis.core.mail.internal.send.SendSmtpTransport;
import ch.elexis.core.mail.model.AuthMode;
import ch.elexis.core.mail.model.MailAccountTestOutcome;
import ch.elexis.core.mail.model.MailAccountTestResult;
import ch.elexis.core.mail.model.MailSecurity;
import ch.elexis.core.mail.model.MailboxAccount;
import ch.elexis.core.mail.model.MailboxScope;
import ch.elexis.core.mail.service.IMailAccountTester;
import ch.elexis.core.mail.service.IMailboxAccountService;
import ch.elexis.core.mail.service.MailAccessException;

import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.MessagingException;
import jakarta.mail.Service;
import jakarta.mail.Session;
import jakarta.mail.URLName;

/** Tests independent IMAP and SMTP authentication without sharing work connections. */
@Component(service = IMailAccountTester.class)
public class MailAccountTester implements IMailAccountTester {
	private final Logger log = LoggerFactory.getLogger(getClass());
	@Reference
	private IMailboxAccountService accountService;
	@Reference
	private OAuth2TokenCache tokens;
	@Reference
	private IConfigService configService;
	@Reference(target = "(service.model.name=ch.elexis.core.model)")
	private IModelService modelService;
	private MailCredentialProvider credentials;
	private final ThreadFactory threads = Thread.ofPlatform().daemon(true).name("mail-account-test-", 0).factory();
	private final ExecutorService executor = Executors.newFixedThreadPool(2, runnable -> {
		Thread thread = threads.newThread(runnable);
		thread.setContextClassLoader(IMAPStore.class.getClassLoader());
		return thread;
	});

	@Activate
	void activate() {
		CredentialCipher cipher = new CredentialCipher();
		credentials = new MailCredentialProvider(new PasswordCredentialProvider(configService, cipher,
				id -> modelService.load(id, IUser.class), id -> modelService.load(id, IContact.class)), tokens);
	}

	/** {@inheritDoc} */
	@Override
	public CompletableFuture<MailAccountTestResult> test(IUser user, MailboxAccount account, char[] password) {
		char[] supplied = password == null ? null : password.clone();
		if (password != null) {
			Arrays.fill(password, '\0');
		}
		try {
			checkManage(user, account);
			MailboxAccountService.validate(account, account.id() != null && accountService.isLegacy(account.id()));
			return CompletableFuture.supplyAsync(() -> {
				try {
					checkManage(user, account);
					if (account.authMode() == AuthMode.OAUTH2 && (account.id() == null
							|| !hasToken(account))) {
						return new MailAccountTestResult(account.imap() == null ? null : MailAccountTestOutcome.NOT_SIGNED_IN,
								account.smtp() == null ? null : MailAccountTestOutcome.NOT_SIGNED_IN);
					}
					return new MailAccountTestResult(probe(account, supplied, false), probe(account, supplied, true));
				} finally {
					clear(supplied);
				}
			}, executor).exceptionally(failure -> {
				log.error("Mail account test failed", new IllegalStateException("Mail account test task failed"));
				throw new CompletionException(failure);
			});
		} catch (RejectedExecutionException e) {
			clear(supplied);
			log.debug("Mail account test rejected after shutdown");
			return CompletableFuture.failedFuture(new IllegalStateException("Mail account tester is disabled"));
		} catch (RuntimeException e) {
			clear(supplied);
			throw e;
		}
	}

	private boolean hasToken(MailboxAccount account) {
		String stored = tokens.store().read(account, MailClientConfig.refreshToken(account.id()));
		return stored != null && !stored.isEmpty();
	}

	private void checkManage(IUser user, MailboxAccount account) {
		if (user == null || !(account.scope() == MailboxScope.PERSONAL
				? user.getId().equals(account.ownerUserId()) : accountService.canManageShared(user))) {
			throw new MailAccessException("Mail account management denied");
		}
		if (account.id() != null && accountService.getManageableAccounts(user).stream()
				.noneMatch(saved -> saved.id().equals(account.id()))) {
			throw new MailAccessException("Mail account management denied");
		}
		if (account.id() == null) {
			accountService.getManageableAccounts(user);
		}
	}

	private MailAccountTestOutcome probe(MailboxAccount account, char[] password, boolean smtp) {
		if ((smtp ? account.smtp() : account.imap()) == null) {
			return null;
		}
		MailSessionFactory sessions = new MailSessionFactory(accountService::isLegacy);
		Service connection;
		if (smtp) {
			Session session = sessions.createSmtpSession(account);
			connection = new SendSmtpTransport(session, new URLName("smtp", account.smtp().host(), account.smtp().port(),
					null, null, null), account.smtp().security() == MailSecurity.SSL);
		} else {
			connection = sessions.createStore(account);
		}
		try {
			credentials.connect(account, smtp, password, (username, secret) -> connection.connect(
					smtp ? account.smtp().host() : account.imap().host(),
					smtp ? account.smtp().port() : account.imap().port(), username, new String(secret)));
			return MailAccountTestOutcome.OK;
		} catch (MessagingException e) {
			return outcome(e);
		} finally {
			try {
				connection.close();
			} catch (MessagingException e) {
				log.debug("Mail test connection cleanup failed");
			}
		}
	}

	private MailAccountTestOutcome outcome(Throwable error) {
		for (Throwable cause = error; cause != null; cause = cause.getCause()) {
			if (cause instanceof AuthenticationFailedException) {
				return MailAccountTestOutcome.AUTH_FAILED;
			}
			if (cause instanceof SSLException) {
				return MailAccountTestOutcome.TLS_FAILED;
			}
			if (cause instanceof UnknownHostException || cause instanceof ConnectException
					|| cause instanceof SocketTimeoutException) {
				return MailAccountTestOutcome.UNREACHABLE;
			}
		}
		return MailAccountTestOutcome.FAILED;
	}

	private void clear(char[] password) {
		if (password != null) {
			Arrays.fill(password, '\0');
		}
	}

	@Deactivate
	void deactivate() {
		executor.shutdown();
	}
}
