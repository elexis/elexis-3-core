package ch.elexis.core.mail;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.apache.commons.lang3.Strings;

import ch.elexis.core.mail.model.AuthMode;
import ch.elexis.core.mail.model.MailProvider;
import ch.elexis.core.mail.model.MailSecurity;
import ch.elexis.core.mail.model.MailboxAccount;
import ch.elexis.core.mail.model.MailboxScope;
import ch.elexis.core.mail.model.SentCopyMode;
import ch.elexis.core.mail.model.ServerEndpoint;

/** Supplies editable account defaults without reading secrets or opening connections. */
public final class MailAccountPresets {
	private MailAccountPresets() {
	}

	/**
	 * Creates a new unsaved account for a provider.
	 * @param provider the provider
	 * @param emailAddress the mailbox address and initial display/login name
	 * @param scope the account scope
	 * @param ownerUserId the owner of a personal account
	 * @return the editable provider preset
	 * @throws NullPointerException if provider or address is null
	 */
	public static MailboxAccount preset(MailProvider provider, String emailAddress, MailboxScope scope,
			String ownerUserId) {
		Objects.requireNonNull(provider, "Mail provider required");
		Objects.requireNonNull(emailAddress, "Mail address required");
		String imap = switch (provider) {
		case IMAP -> "";
		case HIN -> "imap.mail.hin.ch";
		case M365 -> "outlook.office365.com";
		case GOOGLE -> "imap.gmail.com";
		};
		String smtp = switch (provider) {
		case IMAP -> "";
		case HIN -> "smtp.mail.hin.ch";
		case M365 -> personalMicrosoft(emailAddress) ? "smtp-mail.outlook.com" : "smtp.office365.com";
		case GOOGLE -> "smtp.gmail.com";
		};
		SentCopyMode copy = switch (provider) {
		case IMAP -> SentCopyMode.AUTO;
		case HIN -> SentCopyMode.CLIENT;
		case M365, GOOGLE -> SentCopyMode.SERVER;
		};
		boolean oauth = provider == MailProvider.M365 || provider == MailProvider.GOOGLE;
		return new MailboxAccount(null, emailAddress, emailAddress, provider, scope, ownerUserId,
				List.of(), List.of(), List.of(), new ServerEndpoint(imap, 993, MailSecurity.SSL),
				new ServerEndpoint(smtp, provider == MailProvider.GOOGLE ? 465 : 587,
						provider == MailProvider.GOOGLE ? MailSecurity.SSL : MailSecurity.STARTTLS),
				emailAddress, oauth ? AuthMode.OAUTH2 : AuthMode.PASSWORD, false, "", copy, 2, 120);
	}

	/**
	 * Guesses a provider from a legacy server host.
	 * @param host the host, possibly null
	 * @return the provider, or IMAP if no known provider matches
	 */
	public static MailProvider guess(String host) {
		if (Strings.CI.contains(host, "office365.com") || Strings.CI.contains(host, "outlook.com")) {
			return MailProvider.M365;
		}
		if (Strings.CI.contains(host, "gmail.com") || Strings.CI.contains(host, "googlemail.com")) {
			return MailProvider.GOOGLE;
		}
		return Strings.CI.contains(host, "hin.ch") ? MailProvider.HIN : MailProvider.IMAP;
	}

	private static boolean personalMicrosoft(String address) {
		String domain = address.substring(address.lastIndexOf('@') + 1).toLowerCase(Locale.ROOT);
		return domain.startsWith("outlook.") || domain.startsWith("hotmail.") || domain.startsWith("live.")
				|| domain.equals("msn.com");
	}
}
