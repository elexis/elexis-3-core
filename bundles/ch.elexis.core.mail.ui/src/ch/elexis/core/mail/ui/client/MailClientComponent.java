package ch.elexis.core.mail.ui.client;

import org.apache.commons.lang3.StringUtils;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.mail.IMailClient;
import ch.elexis.core.mail.IMailClient.ErrorTyp;
import ch.elexis.core.mail.MailAccount;

@Component
public class MailClientComponent {

	private static String virtLocalId = " [virt_local]";

	private static IMailClient mailClient;

	@Reference
	public void setMailClient(IMailClient mailClient) {
		MailClientComponent.mailClient = mailClient;
	}

	public void unsetMailClient(IMailClient mailClient) {
		MailClientComponent.mailClient = null;
	}

	public static IMailClient getMailClient() {
		return mailClient;
	}

	public static String getLastErrorMessage() {
		String message = StringUtils.EMPTY;
		if (mailClient != null) {
			Optional<ErrorTyp> error = mailClient.getLastError();
			if (error.isPresent()) {
				switch (error.get()) {
				case AUTHENTICATION:
					message = "Fehler bei der Authorisation. Bitte Anmeldedaten überprüfen.";
					break;
				case CONFIGTYP:
					message = "Fehler bei der Konfiguration. Bitte Account Typ überprüfen.";
					break;
				case CONNECTION:
					message = "Fehler bei der Verbindung. Bitte Verbindungsdaten überprüfen.";
					break;
				case ADDRESS:
					message = "Fehler bei der Adresse. Bitte Username überprüfen.";
					break;
				}
			}
		}
		return message;
	}

	public static boolean isVirtLocal(String accountId) {
		return accountId.endsWith(virtLocalId);
	}

	public static boolean isVirtLocal(MailAccount account) {
		return isVirtLocal(account.getId());
	}

	public static String getVirtLocalId(MailAccount account) {
		return account.getId() + virtLocalId;
	}

}
