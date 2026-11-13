package ch.elexis.core.mail.internal.connection;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.DateTimeException;

import org.eclipse.angus.mail.imap.IMAPStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import ch.elexis.core.services.IConfigService;
import ch.elexis.core.mail.MailClientConfig;
import ch.elexis.core.mail.model.MailQuota;
import ch.elexis.core.mail.model.MailQuotaStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.Quota;

/** Maintains one account's quota on its existing work thread without scheduling additional work. */
final class MailQuotaCache {
	private static final Logger log = LoggerFactory.getLogger(MailQuotaCache.class);
	private final IConfigService config;
	private final Clock clock;
	private final String key;
	private final Gson gson = new Gson();
	private volatile MailQuota current;
	private Instant lastAttempt;

	MailQuotaCache(IConfigService config, Clock clock, String accountId) {
		this.config = config;
		this.clock = clock;
		key = MailClientConfig.quota(accountId);
	}

	MailQuota get() {
		MailQuota value = current;
		return value == null ? stored(config, key) : value;
	}

	void refresh(IMAPStore store) {
		Instant now = clock.instant();
		if (lastAttempt != null && now.isBefore(lastAttempt.plus(Duration.ofHours(1)))) {
			return;
		}
		lastAttempt = now;
		try {
			if (!store.hasCapability("QUOTA")) {
				current = new MailQuota(MailQuotaStatus.UNSUPPORTED, -1, -1, now);
				return;
			}
			Quota[] quotas = store.getQuota("INBOX");
			for (Quota quota : quotas) {
				if (quota.resources == null) {
					continue;
				}
				for (Quota.Resource resource : quota.resources) {
					if ("STORAGE".equalsIgnoreCase(resource.name) && resource.usage >= 0 && resource.limit >= 0) {
						MailQuota value = new MailQuota(MailQuotaStatus.OK, resource.usage, resource.limit, now);
						current = value;
						persist(value);
						return;
					}
				}
			}
			current = new MailQuota(MailQuotaStatus.UNKNOWN, -1, -1, now);
		} catch (MessagingException e) {
			if (current == null) {
				current = stored(config, key);
			}
			log.warn("Mail quota could not be read");
		}
	}

	private void persist(MailQuota value) {
		MailQuota previous = stored(config, key);
		if (previous.status() == MailQuotaStatus.OK && previous.usedKb() == value.usedKb()
				&& previous.limitKb() == value.limitKb()
				&& previous.checkedAt().getEpochSecond() / 3600 == value.checkedAt().getEpochSecond() / 3600) {
			return;
		}
		JsonObject json = new JsonObject();
		json.addProperty("usedKb", value.usedKb());
		json.addProperty("limitKb", value.limitKb());
		json.addProperty("checkedAt", value.checkedAt().toString());
		if (!config.set(key, gson.toJson(json), false)) {
			log.warn("Mail quota could not be persisted");
		}
	}

	static MailQuota stored(IConfigService config, String key) {
		String raw = config.get(key, null);
		if (raw != null) {
			try {
				JsonObject json = new Gson().fromJson(raw, JsonObject.class);
				long used = json.get("usedKb").getAsLong();
				long limit = json.get("limitKb").getAsLong();
				Instant checked = Instant.parse(json.get("checkedAt").getAsString());
				if (used >= 0 && limit >= 0) {
					return new MailQuota(MailQuotaStatus.OK, used, limit, checked);
				}
			} catch (JsonParseException | DateTimeException | IllegalStateException | NumberFormatException
					| NullPointerException e) {
				log.warn("Stored mail quota is invalid");
			}
		}
		return new MailQuota(MailQuotaStatus.UNKNOWN, -1, -1, null);
	}
}
