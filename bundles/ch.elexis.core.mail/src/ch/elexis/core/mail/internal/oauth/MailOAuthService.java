package ch.elexis.core.mail.internal.oauth;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.jsoup.nodes.Entities;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import ch.elexis.core.model.IUser;
import ch.elexis.core.mail.MailClientConfig;
import ch.elexis.core.mail.MailEventTopics;
import ch.elexis.core.mail.messages.Messages;
import ch.elexis.core.mail.model.AuthMode;
import ch.elexis.core.mail.model.MailOAuthSignIn;
import ch.elexis.core.mail.model.MailProvider;
import ch.elexis.core.mail.model.MailboxAccount;
import ch.elexis.core.mail.model.MailboxScope;
import ch.elexis.core.mail.service.IMailOAuthService;
import ch.elexis.core.mail.service.IMailboxAccountService;
import ch.elexis.core.mail.service.MailAccessException;
import ch.elexis.core.mail.service.MailOAuthError;
import ch.elexis.core.mail.service.MailOAuthException;

/** Runs isolated, cancellable PKCE sign-ins through the system browser and an IPv4 loopback listener. */
@Component(service = IMailOAuthService.class)
public class MailOAuthService implements IMailOAuthService {
	private final Logger log = LoggerFactory.getLogger(getClass());
	@Reference
	private IMailboxAccountService accountService;
	@Reference
	private OAuth2TokenCache tokens;
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
	private volatile EventAdmin eventAdmin;
	private final Map<String, Flow> flows = new ConcurrentHashMap<>();
	private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor(
			Thread.ofPlatform().daemon(true).name("mail-oauth-timeout").factory());
	private Duration timeout = Duration.ofMinutes(5);
	private boolean stopped;

	/** {@inheritDoc} */
	@Override
	public boolean isAvailable(MailProvider provider) {
		return (provider == MailProvider.M365 || provider == MailProvider.GOOGLE)
				&& tokens.client().isConfigured(provider);
	}

	/** {@inheritDoc} */
	@Override
	public synchronized CompletableFuture<MailOAuthSignIn> signIn(IUser user, String accountId,
			Consumer<URI> openBrowser) {
		MailboxAccount account = manageable(user, accountId);
		if (!isAvailable(account.provider())) {
			return CompletableFuture.failedFuture(new MailOAuthException(MailOAuthError.NOT_CONFIGURED));
		}
		if (stopped) {
			return CompletableFuture.failedFuture(new MailOAuthException(MailOAuthError.CANCELLED));
		}
		Flow previous = flows.remove(accountId);
		if (previous != null) {
			previous.fail(MailOAuthError.CANCELLED);
		}
		try {
			Flow flow = new Flow(user, account);
			flows.put(accountId, flow);
			flow.future.whenComplete((result, failure) -> flow.close());
			flow.deadline = timer.schedule(() -> flow.fail(MailOAuthError.TIMEOUT), timeout.toMillis(),
					TimeUnit.MILLISECONDS);
			flow.server.start();
			try {
				openBrowser.accept(flow.authorize());
			} catch (RuntimeException e) {
				flow.fail(MailOAuthError.CANCELLED);
			}
			return flow.future;
		} catch (IOException e) {
			return CompletableFuture.failedFuture(new MailOAuthException(MailOAuthError.OFFLINE));
		}
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void signOut(IUser user, String accountId) {
		MailboxAccount account = manageable(user, accountId);
		Flow flow = flows.remove(accountId);
		if (flow != null) {
			flow.fail(MailOAuthError.CANCELLED);
		}
		synchronized (tokens) {
			tokens.store().write(account, MailClientConfig.refreshToken(accountId), null);
			tokens.store().write(account, MailClientConfig.signedInAs(accountId), null);
			tokens.clear(accountId);
		}
		changed(accountId);
	}

	/** {@inheritDoc} */
	@Override
	public Optional<String> getSignedInAs(IUser user, String accountId) {
		MailboxAccount account = accountService.getVisibleAccounts(user).stream()
				.filter(candidate -> candidate.id().equals(accountId)).findFirst()
				.orElseThrow(() -> new MailAccessException("Mail account access denied"));
		return Optional.ofNullable(tokens.store().read(account, MailClientConfig.signedInAs(accountId)))
				.filter(value -> !value.isBlank());
	}

	private MailboxAccount manageable(IUser user, String accountId) {
		MailboxAccount account = accountService.getAccount(accountId)
				.orElseThrow(() -> new IllegalArgumentException("Unknown mail account"));
		if (accountService.getManageableAccounts(user).stream().noneMatch(value -> value.id().equals(accountId))
				|| account.scope() == MailboxScope.PERSONAL && !user.getId().equals(account.ownerUserId())) {
			throw new MailAccessException("Mail account management denied");
		}
		if (account.authMode() != AuthMode.OAUTH2
				|| account.provider() != MailProvider.M365 && account.provider() != MailProvider.GOOGLE) {
			throw new IllegalArgumentException("Supported OAuth account required");
		}
		return account;
	}

	private void changed(String accountId) {
		EventAdmin events = eventAdmin;
		if (events != null) {
			try {
				events.postEvent(new Event(MailEventTopics.ACCOUNTS, Map.of(MailEventTopics.ACCOUNT_ID, accountId)));
			} catch (IllegalStateException e) {
				log.warn("Mail account event service is unavailable");
			}
		}
	}

	static String verifier() {
		return random(32);
	}

	static String state() {
		return random(16);
	}

	private static String random(int bytes) {
		byte[] value = new byte[bytes];
		new SecureRandom().nextBytes(value);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
	}

	static String challenge(String verifier) {
		try {
			return Base64.getUrlEncoder().withoutPadding()
					.encodeToString(MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII)));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 unavailable", e);
		}
	}

	private String identity(String jwt) throws MailOAuthException {
		try {
			JWTClaimsSet claims = JWTParser.parse(jwt).getJWTClaimsSet();
			String address = claims.getStringClaim("email");
			if (address == null || address.isBlank()) {
				address = claims.getStringClaim("preferred_username");
			}
			if (address == null || address.isBlank()) {
				throw new MailOAuthException(MailOAuthError.TOKEN_FAILED);
			}
			return address;
		} catch (ParseException e) {
			throw new MailOAuthException(MailOAuthError.TOKEN_FAILED);
		}
	}

	@Deactivate
	synchronized void deactivate() {
		stopped = true;
		for (Flow flow : flows.values()) {
			flow.fail(MailOAuthError.CANCELLED);
		}
		timer.shutdownNow();
	}

	private final class Flow {
		private final IUser user;
		private final MailboxAccount account;
		private final String verifier = verifier();
		private final String state = state();
		private final HttpServer server;
		private final ExecutorService executor = Executors.newSingleThreadExecutor(
				Thread.ofPlatform().daemon(true).name("mail-oauth-callback").factory());
		private final CompletableFuture<MailOAuthSignIn> future = new CompletableFuture<>() {
			/**
			 * Cancels the flow atomically with token persistence.
			 * @param mayInterruptIfRunning whether interruption was requested
			 * @return whether cancellation completed this future
			 */
			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				synchronized (Flow.this) {
					if (!isDone()) {
						close();
					}
					return super.cancel(mayInterruptIfRunning);
				}
			}
		};
		private final AtomicBoolean claimed = new AtomicBoolean();
		private ScheduledFuture<?> deadline;

		private Flow(IUser user, MailboxAccount account) throws IOException {
			this.user = user;
			this.account = account;
			server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
			server.setExecutor(executor);
			server.createContext("/", this::callback);
		}

		private String redirect() {
			return "http://" + (account.provider() == MailProvider.M365 ? "localhost" : "127.0.0.1")
					+ ":" + server.getAddress().getPort() + "/";
		}

		private URI authorize() {
			Map<String, String> query = new LinkedHashMap<>();
			query.put("client_id", tokens.client().clientId(account.provider()));
			query.put("response_type", "code");
			query.put("redirect_uri", redirect());
			query.put("scope", tokens.client().scope(account.provider()));
			query.put("code_challenge", challenge(verifier));
			query.put("code_challenge_method", "S256");
			query.put("state", state);
			if (account.provider() == MailProvider.GOOGLE || !account.oauthSharedMailbox()) {
				query.put("login_hint", account.emailAddress());
			}
			query.put("prompt", account.provider() == MailProvider.GOOGLE ? "consent" : "select_account");
			if (account.provider() == MailProvider.GOOGLE) {
				query.put("access_type", "offline");
			}
			return URI.create(tokens.client().authorize(account.provider()) + "?" + OAuth2Client.encode(query));
		}

		private void callback(HttpExchange exchange) throws IOException {
			Map<String, String> query = query(exchange.getRequestURI().getRawQuery());
			if (!"GET".equals(exchange.getRequestMethod()) || !"/".equals(exchange.getRequestURI().getRawPath())
					|| !query.containsKey("state") || !query.containsKey("code") && !query.containsKey("error")
					|| !claimed.compareAndSet(false, true)) {
				exchange.sendResponseHeaders(404, -1);
				exchange.close();
				return;
			}
			MailOAuthError failure = null;
			MailOAuthSignIn result = null;
			try {
				if (!state.equals(query.get("state"))) {
					throw new MailOAuthException(MailOAuthError.STATE_MISMATCH);
				}
				if (query.containsKey("error")) {
					String error = query.get("error");
					throw new MailOAuthException("access_denied".equals(error) || "consent_required".equals(error)
							|| "interaction_required".equals(error) ? MailOAuthError.DENIED : MailOAuthError.TOKEN_FAILED);
				}
				String code = query.get("code");
				if (code == null || code.isBlank()) {
					throw new MailOAuthException(MailOAuthError.TOKEN_FAILED);
				}
				OAuth2Client.Token token = tokens.client().request(account.provider(), Map.of("grant_type",
						"authorization_code", "code", code, "redirect_uri", redirect(), "code_verifier", verifier));
				String signedInAs = identity(token.identity());
				if (token.refresh().isBlank()) {
					throw new MailOAuthException(MailOAuthError.TOKEN_FAILED);
				}
				synchronized (this) {
					if (future.isDone() || !account.equals(manageable(user, account.id()))) {
						throw new MailOAuthException(MailOAuthError.CANCELLED);
					}
					synchronized (tokens) {
						tokens.store().refreshToken(account, token.refresh());
						tokens.store().write(account, MailClientConfig.signedInAs(account.id()), signedInAs);
						tokens.clear(account.id());
					}
					result = new MailOAuthSignIn(account.id(), signedInAs);
					changed(account.id());
					page(exchange, true);
					close();
					future.complete(result);
				}
			} catch (MailOAuthException e) {
				failure = e.reason();
			} catch (GeneralSecurityException | IllegalArgumentException | IllegalStateException | MailAccessException e) {
				failure = MailOAuthError.TOKEN_FAILED;
			} finally {
				if (result == null) {
					try {
						page(exchange, false);
					} finally {
						fail(failure == null ? MailOAuthError.TOKEN_FAILED : failure);
					}
				} else if (!future.isDone()) {
					future.complete(result);
				}
			}
		}

		private Map<String, String> query(String raw) {
			Map<String, String> values = new HashMap<>();
			if (raw != null) {
				try {
					for (String part : raw.split("&")) {
						String[] pair = part.split("=", 2);
						if (pair.length == 2 && values.put(URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
								URLDecoder.decode(pair[1], StandardCharsets.UTF_8)) != null) {
							return Map.of();
						}
					}
				} catch (IllegalArgumentException e) {
					return Map.of();
				}
			}
			return values;
		}

		private void page(HttpExchange exchange, boolean success) throws IOException {
			String text = success ? Messages.OAuthSuccess : Messages.OAuthFailure;
			byte[] body = ("<!doctype html><html><head><meta charset=\"utf-8\"></head><body><p>"
					+ Entities.escape(text) + "</p></body></html>").getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
			exchange.getResponseHeaders().set("Cache-Control", "no-store");
			exchange.getResponseHeaders().set("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'");
			exchange.sendResponseHeaders(200, body.length);
			try (var output = exchange.getResponseBody()) {
				output.write(body);
			} finally {
				exchange.close();
			}
		}

		private synchronized void fail(MailOAuthError reason) {
			close();
			future.completeExceptionally(new MailOAuthException(reason));
		}

		private void close() {
			flows.remove(account.id(), this);
			ScheduledFuture<?> task = deadline;
			if (task != null) {
				task.cancel(false);
			}
			server.stop(0);
			executor.shutdownNow();
		}
	}
}
