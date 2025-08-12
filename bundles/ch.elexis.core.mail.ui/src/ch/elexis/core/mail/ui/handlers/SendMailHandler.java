package ch.elexis.core.mail.ui.handlers;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.mail.AttachmentsUtil;
import ch.elexis.core.mail.MailMessage;
import ch.elexis.core.mail.TaskUtil;
import ch.elexis.core.mail.ui.dialogs.SendMailDialog;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IDocumentLetter;
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
			List<IDocument> loadedDocuments = AttachmentsUtil.getDocuments(documents);
			for (IDocument iDocument : loadedDocuments) {
				if (iDocument instanceof IDocumentLetter && ((IDocumentLetter) iDocument).getRecipient() != null
						&& StringUtils.isNotBlank(((IDocumentLetter) iDocument).getRecipient().getEmail())) {
					sendMailDialog.setTo(((IDocumentLetter) iDocument).getRecipient().getEmail());
					break;
				}
			}
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

		Boolean doSend = Boolean.TRUE;
		String doSendString = event.getParameter("ch.elexis.core.mail.ui.sendMail.doSend");
		if (StringUtils.isNotBlank(doSendString)) {
			doSend = Boolean.valueOf(doSendString);
			sendMailDialog.doSend(doSend);
		}

		String patId = event.getParameter("ch.elexis.core.mail.ui.sendMail.patId");
		if (StringUtils.isNotBlank(patId)) {
			sendMailDialog.setPatID(patId);
		}

		if (sendMailDialog.open() == Dialog.OK) {
			if (doSend && taskDescriptor != null && taskDescriptor.isPresent()) {
				ContextServiceHolder.get().getRootContext().setNamed("mail.alreadySent", true);
			} else {
				ContextServiceHolder.get().getRootContext().setNamed("mail.alreadySent", false);
			}

			MailMessage message = new MailMessage().to(sendMailDialog.getTo()).cc(sendMailDialog.getCc())
					.subject(sendMailDialog.getSubject()).text(sendMailDialog.getText());
			message.setAttachments(sendMailDialog.getAttachmentsString());
			message.setDocuments(sendMailDialog.getDocumentsString());
			taskDescriptor = TaskUtil
					.createSendMailTaskDescriptor(sendMailDialog.getAccount().getId(), message);
			if (doSend && taskDescriptor.isPresent()) {
				ContextServiceHolder.get().getRootContext().setNamed("sendMailDialog.taskDescriptor", null);
				ITask task = new SendMailTaskWithProgress().execute(HandlerUtil.getActiveShell(event),
						taskDescriptor.get(), message);
				return task.getState() == TaskState.COMPLETED;
			} else if (!doSend) {
				ContextServiceHolder.get().getRootContext().setNamed("sendMailDialog.taskDescriptor",
						taskDescriptor.get());
			}
		}
		return false;
	}
}