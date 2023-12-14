package ch.elexis.core.mail.ui.handlers;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.mail.MailMessage;
import ch.elexis.core.mail.TaskUtil;
import ch.elexis.core.mail.ui.dialogs.SendMailDialog;
import ch.elexis.core.services.holder.ContextServiceHolder;
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
	public static Optional<ITaskDescriptor> taskDescriptor;
	public static final String MESSAGE_KEY = "message";
	public static final String TEXT_KEY = "text";
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
		String accountId = event.getParameter("ch.elexis.core.mail.ui.sendMail.accountid");
		if (to != null) {
			sendMailDialog.setAccountId(accountId);
		}
		String subject = event.getParameter("ch.elexis.core.mail.ui.sendMail.subject");
		if (subject != null) {
			sendMailDialog.setSubject(subject);
		}
		String text = event.getParameter("ch.elexis.core.mail.ui.sendMail.text");
		Optional<?> descriptor = ContextServiceHolder.get().getNamed("sendMailDialog.taskDescriptor");
		if (descriptor.isPresent() && descriptor.get() instanceof ITaskDescriptor) {
			ITaskDescriptor taskDescriptor = (ITaskDescriptor) descriptor.get();
			Map<String, Serializable> runContext = taskDescriptor.getRunContext();
			if (runContext != null && runContext.containsKey(MESSAGE_KEY)) {
				Object messageObject = runContext.get(MESSAGE_KEY);
				if (messageObject instanceof Map) {
					Map<?, ?> messageMap = (Map<?, ?>) messageObject;
					Object messageText = messageMap.get(TEXT_KEY);
					if (messageText != null) {
						text = messageText.toString();
					}
				}
			}
		}
		if (text != null) {
			sendMailDialog.setText(text);
		}

		String doSendString = event.getParameter("ch.elexis.core.mail.ui.sendMail.doSend");
		if (doSendString != null) {
			Boolean doSend = Boolean.valueOf(doSendString);
			sendMailDialog.doSend(doSend);
		}
		if (sendMailDialog.open() == Dialog.OK) {
			MailMessage message = new MailMessage().to(sendMailDialog.getTo()).cc(sendMailDialog.getCc())
					.subject(sendMailDialog.getSubject()).text(sendMailDialog.getText());
			message.setAttachments(sendMailDialog.getAttachmentsString());
			message.setDocuments(sendMailDialog.getDocumentsString());
			taskDescriptor = TaskUtil
					.createSendMailTaskDescriptor(sendMailDialog.getAccount().getId(), message);
			ContextServiceHolder.get().getRootContext().setNamed("sendMailDialog.taskDescriptor", taskDescriptor.get());
			if (!Boolean.valueOf(doSendString) && taskDescriptor.isPresent()) {
				ITask task = new SendMailTaskWithProgress().execute(HandlerUtil.getActiveShell(event),
						taskDescriptor.get());
				return task.getState() == TaskState.COMPLETED;
			}
		}
		return false;
	}
}