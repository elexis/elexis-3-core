package ch.elexis.core.mail.internal.oauth;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import ch.elexis.core.mail.MailOAuthConstants;
import ch.elexis.core.mail.model.MailProvider;
import ch.elexis.core.mail.service.MailOAuthError;
import ch.elexis.core.mail.service.MailOAuthException;

final class OAuth2Client {
	private final Properties properties = new Properties();
	private final ExecutorService executor = Executors.newFixedThreadPool(2,
			Thread.ofPlatform().daemon(true).name("mail-oauth-http-", 0).factory());
	private final HttpClient http = HttpClient.newBuilder().executor(executor)
			.connectTimeout(Duration.ofSeconds(15)).build();
	private URI authorizeOverride;
	private URI tokenOverride;

	OAuth2Client() {
		try (InputStream input = getClass().getResourceAsStream("/rsc/id.properties")) {
			if (input != null) {
				properties.load(input);
			}
		} catch (IOException e) {
			properties.clear();
			LoggerFactory.getLogger(getClass()).error("Error loading mail OAuth client properties", e);
		}
	}

	void close() {
		http.shutdownNow();
		executor.shutdownNow();
	}

	String clientId(MailProvider provider) {
		return provider == MailProvider.M365 ? properties.getProperty("microsoft_client_id", "").strip()
				: provider == MailProvider.GOOGLE ? properties.getProperty("google_client_id", "").strip() : "";
	}

	boolean isConfigured(MailProvider provider) {
		return !clientId(provider).isBlank()
				&& (provider != MailProvider.GOOGLE || !properties.getProperty("google_client_secret", "").isBlank());
	}

	String scope(MailProvider provider) {
		return provider == MailProvider.M365 ? MailOAuthConstants.MICROSOFT_SCOPE : MailOAuthConstants.GOOGLE_SCOPE;
	}

	URI authorize(MailProvider provider) {
		return authorizeOverride != null ? authorizeOverride : endpoint(provider, true);
	}

	private URI endpoint(MailProvider provider, boolean authorize) {
		if (provider == MailProvider.GOOGLE) {
			return URI.create(authorize ? MailOAuthConstants.GOOGLE_AUTHORIZE : MailOAuthConstants.GOOGLE_TOKEN);
		}
		return URI.create(authorize ? MailOAuthConstants.MICROSOFT_AUTHORIZE : MailOAuthConstants.MICROSOFT_TOKEN);
	}

	Token request(MailProvider provider, Map<String, String> values) throws MailOAuthException {
		Map<String, String> form = new LinkedHashMap<>(values);
		String clientId = clientId(provider);
		form.put("client_id", clientId);
		if (!isConfigured(provider)) {
			throw new MailOAuthException(MailOAuthError.NOT_CONFIGURED);
		}
		try {
			if (provider == MailProvider.GOOGLE) {
				form.put("client_secret", properties.getProperty("google_client_secret", ""));
			}
			URI endpoint = tokenOverride != null ? tokenOverride : endpoint(provider, false);
			HttpRequest request = HttpRequest.newBuilder(endpoint).timeout(Duration.ofSeconds(30))
					.header("Content-Type", "application/x-www-form-urlencoded")
					.POST(HttpRequest.BodyPublishers.ofString(encode(form))).build();
			HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
			JsonObject json;
			try {
				json = JsonParser.parseString(response.body()).getAsJsonObject();
			} catch (JsonParseException | IllegalStateException e) {
				throw new TokenFailure(response.statusCode() == 400 || response.statusCode() == 401);
			}
			String error = string(json, "error");
			if (response.statusCode() != 200 || !error.isEmpty()) {
				throw new TokenFailure(response.statusCode() == 400 || response.statusCode() == 401
						|| "invalid_grant".equals(error));
			}
			String access = string(json, "access_token");
			long expiry = json.has("expires_in") ? json.get("expires_in").getAsLong() : 3600;
			if (access.isEmpty() || expiry <= 0) {
				throw new TokenFailure(false);
			}
			return new Token(access, string(json, "refresh_token"), string(json, "id_token"), expiry);
		} catch (IOException e) {
			throw new MailOAuthException(MailOAuthError.OFFLINE);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new MailOAuthException(MailOAuthError.CANCELLED);
		} catch (JsonParseException | IllegalStateException | IllegalArgumentException
				| UnsupportedOperationException e) {
			throw new MailOAuthException(MailOAuthError.TOKEN_FAILED);
		}
	}

	static String string(JsonObject object, String key) {
		return object.has(key) && object.get(key).isJsonPrimitive() ? object.get(key).getAsString() : "";
	}

	static String encode(Map<String, String> values) {
		return values.entrySet().stream().map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
				+ "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)).collect(Collectors.joining("&"));
	}

	record Token(String access, String refresh, String identity, long expiresIn) {
	}

	static final class TokenFailure extends MailOAuthException {
		private static final long serialVersionUID = 1L;
		private final boolean signInRequired;

		TokenFailure(boolean signInRequired) {
			super(MailOAuthError.TOKEN_FAILED);
			this.signInRequired = signInRequired;
		}

		boolean signInRequired() {
			return signInRequired;
		}
	}
}
