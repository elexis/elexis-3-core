package ch.elexis.core.mail.ui.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;

import ch.elexis.core.mail.MailAccount;
import ch.elexis.core.mail.MailAccount.TYPE;
import ch.elexis.core.mail.MailMessage;
import ch.elexis.core.mail.ui.client.MailClientComponent;
import ch.elexis.data.Mandant;

/**
 * Handler for sending an Email without UI. The command execution returns a String with the result
 * of sending the mail, which is empty on success. The mail can be specified via the command
 * parameters.
 * <li>ch.elexis.core.mail.ui.sendMailNoUi.mandant</li>
 * <li>ch.elexis.core.mail.ui.sendMailNoUi.attachments</li>
 * <li>ch.elexis.core.mail.ui.sendMailNoUi.to</li>
 * <li>ch.elexis.core.mail.ui.sendMailNoUi.subject</li>
 * <li>ch.elexis.core.mail.ui.sendMailNoUi.text</li> <br />
 * Attachments are sent, and not deleted afterwards.
 * 
 * @author thomas
 *
 */
public class SendMailNoUiHandler extends AbstractHandler implements IHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		MailAccount mailAccount =
			getMailAccount(event.getParameter("ch.elexis.core.mail.ui.sendMailNoUi.mandant"));
		if (mailAccount == null) {
			Mandant mandant =
				Mandant.load(event.getParameter("ch.elexis.core.mail.ui.sendMailNoUi.mandant"));
			return "No account for mandant ["
				+ mandant.getLabel(false) + "]";
		}
		
		String attachments = event.getParameter("ch.elexis.core.mail.ui.sendMailNoUi.attachments");
		String to = event.getParameter("ch.elexis.core.mail.ui.sendMailNoUi.to");
		if (StringUtils.isBlank(to)) {
			return "No to address";
		}
		String subject = event.getParameter("ch.elexis.core.mail.ui.sendMailNoUi.subject");
		String text = event.getParameter("ch.elexis.core.mail.ui.sendMailNoUi.text");
		
		MailMessage message = new MailMessage().to(to).subject(StringUtils.defaultString(subject))
			.text(StringUtils.defaultString(text));
		if (attachments != null && !attachments.isEmpty()) {
			List<File> attachmentList = getAttachmentsList(attachments);
			for (File file : attachmentList) {
				message.addAttachment(file);
			}
		}
		if (MailClientComponent.getMailClient().sendMail(mailAccount, message)) {
			return null;
		} else {
			return MailClientComponent.getLastErrorMessage();
		}
	}
	
	private MailAccount getMailAccount(String mandantId){
		List<String> accounts = MailClientComponent.getMailClient().getAccounts();
		for (String string : accounts) {
			Optional<MailAccount> account = MailClientComponent.getMailClient().getAccount(string);
			if (account.isPresent() && account.get().getType() == TYPE.SMTP
				&& account.get().isForMandant(mandantId)) {
				return account.get();
			}
		}
		return null;
	}
	
	private List<File> getAttachmentsList(String attachments){
		List<File> ret = new ArrayList<File>();
		if (attachments != null && !attachments.isEmpty()) {
			String[] parts = attachments.split(":::");
			for (String string : parts) {
				ret.add(new File(string));
			}
		}
		return ret;
	}
}
