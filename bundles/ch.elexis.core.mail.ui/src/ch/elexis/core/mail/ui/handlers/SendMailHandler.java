package ch.elexis.core.mail.ui.handlers;

import java.util.Optional;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ui.handlers.HandlerUtil;
import at.medevit.elexis.agenda.ui.composite.EmailState;
import ch.elexis.core.mail.MailMessage;
import ch.elexis.core.mail.TaskUtil;
import ch.elexis.core.mail.ui.dialogs.SendMailDialog;
import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.TaskState;

/**
 * Handler for sending an Email. The mail can be specified via the command
 * parameters.
 * <li>ch.elexis.core.mail.ui.sendMail.attachments</li>
 * <li>ch.elexis.core.mail.ui.sendMail.documents</li>
 * <li>ch.elexis.core.mail.ui.sendMail.to</li>
 * <li>ch.elexis.core.mail.ui.sendMail.subject</li>
 * <li>ch.elexis.core.mail.ui.sendMail.text</li> <br />
 * Additionally a Dialog is displayed to configure the mail, and the account to
 * use for sending. The execution blocks until the sending is done. Attachments
 * are sent, and not deleted afterwards.
 *
 * @author thomas
 *
 */
public class SendMailHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		SendMailDialog sendMailDialog = new SendMailDialog(HandlerUtil.getActiveShell(event));
		String attachments = event.getParameter("ch.elexis.core.mail.ui.sendMail.attachments");
		if (attachments != null) {
			sendMailDialog.setAttachmentsString(attachments);
		}
		String documents = event.getParameter("ch.elexis.core.mail.ui.sendMail.documents");
		if (documents != null) {
			sendMailDialog.setDocumentsString(documents);
		}
		String to = event.getParameter("ch.elexis.core.mail.ui.sendMail.to");
		if (to != null) {
			sendMailDialog.setTo(to);
		}
		String subject = event.getParameter("ch.elexis.core.mail.ui.sendMail.subject");
		if (subject != null) {
			sendMailDialog.setSubject(subject);
		}
		String hideLabelParam = event.getParameter("ch.elexis.core.mail.ui.sendMail.hideLabel");
		boolean hideLabelValue = "true".equals(hideLabelParam);
		sendMailDialog.setHideLabel(hideLabelValue);

		String account = event.getParameter("ch.elexis.core.mail.ui.sendMail.account");
		if (account != null) {
			sendMailDialog.setAccount(account);
		}
		String text = event.getParameter("ch.elexis.core.mail.ui.sendMail.text");
		if (text != null) {
			sendMailDialog.setText(text);
		}
		String template = event.getParameter("ch.elexis.core.mail.ui.sendMail.emailTemplate");
		if (template != null) {
			sendMailDialog.setTemplate(template);

		}

		String time = event.getParameter("ch.elexis.core.mail.ui.sendMail.time");
		if (time != null) {
			sendMailDialog.setTime(time);

		}
		String bereich = event.getParameter("ch.elexis.core.mail.ui.sendMail.bereich");
		if (bereich != null) {
			sendMailDialog.setBereich(bereich);

		}

		String autoSend1 = event.getParameter("ch.elexis.core.mail.ui.sendMail.autoSend");
		boolean autoSend = "true".equals(autoSend1);
		sendMailDialog.setAutoSend(autoSend);

		if (sendMailDialog.open() == Dialog.OK) {
			MailMessage message = new MailMessage().to(sendMailDialog.getTo()).cc(sendMailDialog.getCc())
					.subject(sendMailDialog.getSubject()).text(sendMailDialog.getText());
			message.setAttachments(sendMailDialog.getAttachmentsString());
			message.setDocuments(sendMailDialog.getDocumentsString());
			Optional<ITaskDescriptor> taskDescriptor = TaskUtil
					.createSendMailTaskDescriptor(sendMailDialog.getAccount().getId(), message);
			if (taskDescriptor.isPresent()) {
				ITask task = new SendMailTaskWithProgress().execute(HandlerUtil.getActiveShell(event),
						taskDescriptor.get());
				EmailState.getInstance().setEmailSent(true);
				return task.getState() == TaskState.COMPLETED;
			}
		}
		return false;
	}
}
