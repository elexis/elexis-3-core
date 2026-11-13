package ch.elexis.core.mail.internal.legacy;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import ch.elexis.core.mail.MailAccount;
import ch.elexis.core.mail.MailAccount.TYPE;
import ch.elexis.core.mail.MailAccountPresets;
import ch.elexis.core.mail.model.AuthMode;
import ch.elexis.core.mail.model.MailSecurity;
import ch.elexis.core.mail.model.MailboxAccount;
import ch.elexis.core.mail.model.MailboxScope;
import ch.elexis.core.mail.model.ServerEndpoint;

/** Converts legacy protocol accounts and password-free compatibility views. */
public final class MailLegacyAccountConverter {

	private MailLegacyAccountConverter() {
	}

	/**
	 * Converts one legacy protocol account to a shared password account.
	 * @param account the legacy account
	 * @return the account with the original id and one endpoint
	 */
	public static MailboxAccount toMailbox(MailAccount account) {
		Objects.requireNonNull(account.getType(), "Mail account type required");
		boolean smtp = account.getType() == TYPE.SMTP;
		String address = account.getFrom() == null || account.getFrom().isBlank()
				? Objects.toString(account.getUsername(), "") : account.getFrom();
		MailboxAccount defaults = MailAccountPresets.preset(MailAccountPresets.guess(account.getHost()), address,
				MailboxScope.SHARED, null);
		MailSecurity security = account.isStarttls() ? MailSecurity.STARTTLS
				: account.getType() == TYPE.IMAPS || smtp && "465".equals(account.getPort())
						? MailSecurity.SSL : MailSecurity.NONE;
		int port = account.getPort() == null || account.getPort().isBlank()
				? smtp ? 25 : account.getType() == TYPE.IMAPS ? 993 : 143 : Integer.parseInt(account.getPort());
		ServerEndpoint endpoint = new ServerEndpoint(Objects.toString(account.getHost(), ""), port, security);
		return new MailboxAccount(account.getId(), account.getId(), address, defaults.provider(), MailboxScope.SHARED,
				null, List.of(), List.of(), List.of(), smtp ? null : endpoint, smtp ? endpoint : null,
				account.getUsername(), AuthMode.PASSWORD, false, defaults.signatureHtml(), defaults.sentCopyMode(),
				defaults.connectionBudget(), defaults.pollIntervalSeconds());
	}

	/**
	 * Builds the compatibility view without exposing credentials.
	 * @param account the stored account
	 * @param mandatorIds the mandator selection
	 * @return the legacy view, preferring SMTP when available
	 */
	public static MailAccount toLegacy(MailboxAccount account, List<String> mandatorIds) {
		ServerEndpoint endpoint = account.smtp() != null ? account.smtp() : account.imap();
		MailAccount result = new MailAccount();
		result.setId(account.id());
		result.setType(account.smtp() != null ? TYPE.SMTP : endpoint.security() == MailSecurity.SSL ? TYPE.IMAPS : TYPE.IMAP);
		result.setHost(endpoint.host());
		result.setPort(Integer.toString(endpoint.port()));
		result.setUsername(account.username());
		result.setFrom(account.emailAddress());
		result.setStarttls(endpoint.security() == MailSecurity.STARTTLS);
		result.setMandants(String.join("||", mandatorIds));
		return result;
	}

	/**
	 * Reads the legacy mandator separator.
	 * @param account the legacy account
	 * @return selected mandator ids
	 */
	public static List<String> mandatorIds(MailAccount account) {
		return account.getMandants() == null ? List.of()
				: Arrays.stream(account.getMandants().split("\\|\\|")).filter(id -> !id.isBlank()).toList();
	}
}
