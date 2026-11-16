package ch.elexis.core.mail.internal.legacy;

import java.security.SecureRandom;
import java.util.Base64;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.mail.MailAccount;
import ch.elexis.core.mail.MailClientConfig;
import ch.elexis.core.model.IConfig;
import ch.elexis.core.services.IAccessControlService;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IModelService;

/** Migrates global legacy accounts once per installation using a database insert claim. */
@Component(service = MailLegacyMigration.class, immediate = true)
public class MailLegacyMigration {
	private static final String MARKER = "mail/client/legacy/migration";
	private final Logger log = LoggerFactory.getLogger(getClass());
	@Reference
	private IConfigService configService;
	@Reference(target = "(service.model.name=ch.elexis.core.model)")
	private IModelService modelService;
	@Reference
	private MailLegacyAccountStore accounts;
	@Reference
	private IAccessControlService accessControlService;

	@Activate
	void activate() {
		accessControlService.doPrivileged(this::migrate);
	}

	private void migrate() {
		if (modelService.load(MARKER, IConfig.class).isPresent()) {
			return;
		}
		try {
			if (modelService.executeNativeUpdate("INSERT INTO config (param, wert) VALUES ('mail/client/legacy/migration', 'running')") != 1) {
				return;
			}
		} catch (RuntimeException e) {
			if (modelService.load(MARKER, IConfig.class, false, true).isEmpty()) {
				log.warn("Legacy mail migration claim failed: {}", e.getClass().getSimpleName());
			}
			return;
		}
		try {
			ensureSalt();
			boolean complete = true;
			for (String id : configService.get("ch.elexis.core.mail/accounts", "").split(",")) {
				if (id.isBlank()) {
					continue;
				}
				try {
					if (configService.get(MailClientConfig.account(id), null) != null) {
						continue;
					}
					String value = configService.get("ch.elexis.core.mail/account/" + id, null);
					if (value == null) {
						throw new IllegalArgumentException("Legacy mail account is missing");
					}
					MailAccount account = MailAccount.from(value);
					if (account == null) {
						throw new IllegalArgumentException("Invalid legacy mail account");
					}
					account.setId(id);
					accounts.save(account);
					log.info("Migrated legacy mail account {}", id);
				} catch (RuntimeException e) {
					complete = false;
					log.warn("Legacy mail migration failed for account {}", id, e);
				}
			}
			if (!complete) {
				modelService.executeNativeUpdate("DELETE FROM config WHERE param = '" + MARKER + "'");
				return;
			}
			IConfig marker = modelService.load(MARKER, IConfig.class, false, true).orElseThrow();
			marker.setValue("done");
			modelService.save(marker);
		} catch (RuntimeException e) {
			log.warn("Legacy mail migration failed", e);
			modelService.executeNativeUpdate("DELETE FROM config WHERE param = '" + MARKER + "'");
		}
	}

	private void ensureSalt() {
		if (configService.get(MailClientConfig.SECRET_SALT, null) == null) {
			byte[] salt = new byte[32];
			new SecureRandom().nextBytes(salt);
			String value = Base64.getEncoder().encodeToString(salt);
			try {
				modelService.executeNativeUpdate("INSERT INTO config (param, wert) VALUES ('mail/client/secret/salt', '" + value + "')");
			} catch (RuntimeException e) {
				if (modelService.load(MailClientConfig.SECRET_SALT, IConfig.class, false, true).isEmpty()) {
					throw e;
				}
			}
		}
	}
}
