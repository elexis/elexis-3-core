package ch.elexis.core.mail.internal;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.mail.IMailClient;
import ch.elexis.core.mail.MailAccount;
import ch.elexis.core.mail.MailMessage;
import ch.elexis.core.mail.PreferenceConstants;
import ch.elexis.core.model.message.TransientMessage;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IMessageTransporter;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.status.ObjectStatus;

@Component
public class MailTransporter implements IMessageTransporter {

	@Reference
	private IConfigService configService;

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@Reference
	private IMailClient mailClient;

	@Override
	public String getUriScheme() {
		return "mailto";
	}

	@Override
	public boolean isExternal() {
		return true;
	}

	@Override
	public IStatus send(TransientMessage transientMessage) {
		String jobID = UUID.randomUUID().toString();
		String receiver = transientMessage.getReceiver();
		String text = transientMessage.getMessageText();
		Map<String, String> messageCodes = transientMessage.getMessageCodes();
		String subject = null;
		String search = "Subject:";

		int index = text.indexOf(search);
		if (index != -1) {
			int startIndex = index + search.length();
			int endIndex = text.indexOf("\n", startIndex);

			if (endIndex == -1) {
				endIndex = text.length();
			}
			subject = text.substring(startIndex, endIndex).trim();
		}
		String onlyText = text.substring(text.indexOf("\n") + 1);

		Optional<MailAccount> account = mailClient.getAccount(messageCodes.get("account.id"));
		if (account.get() == null) {
			String defaultAccount = configService.get(PreferenceConstants.PREF_DEFAULT_MAIL_ACCOUNT, null);
			account = mailClient.getAccount(defaultAccount);
		}

		if (StringUtils.isEmpty(receiver)) {
			return new Status(Status.ERROR, MailTransporter.class.getPackageName(), "Patient hat keine E-Mail Adresse");
		}

		MailMessage message = new MailMessage().to(receiver).subject(subject).text(onlyText);
		mailClient.sendMail(account.get(), message);
		return ObjectStatus.OK_STATUS("mail send: " + jobID);
	}

}
