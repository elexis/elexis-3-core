package ch.elexis.core.mail.ui.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.mail.MailMessage;
import ch.elexis.core.mail.TaskUtil;
import ch.elexis.core.mail.ui.client.MailClientComponent;
import ch.elexis.core.mail.ui.dialogs.SendMailDialog;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.TaskState;

/**
 * Handler for sending an Email. The mail can be specified via the command parameters.
 * <li>ch.elexis.core.mail.ui.sendMail.attachments</li>
 * <li>ch.elexis.core.mail.ui.sendMail.documents</li>
 * <li>ch.elexis.core.mail.ui.sendMail.to</li>
 * <li>ch.elexis.core.mail.ui.sendMail.subject</li>
 * <li>ch.elexis.core.mail.ui.sendMail.text</li> <br />
 * Additionally a Dialog is displayed to configure the mail, and the account to use for sending. The
 * execution blocks until the sending is done. Attachments are sent, and not deleted afterwards.
 * 
 * @author thomas
 *
 */
public class SendMailHandler extends AbstractHandler implements IHandler {
	
	private ITask task;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		SendMailDialog sendMailDialog = new SendMailDialog(HandlerUtil.getActiveShell(event));
		sendMailDialog.create();
		String attachments = event.getParameter("ch.elexis.core.mail.ui.sendMail.attachments");
		if (attachments != null) {
			sendMailDialog.setAttachments(attachments);
		}
		String documents = event.getParameter("ch.elexis.core.mail.ui.sendMail.documents");
		if (documents != null) {
			sendMailDialog.setDocuments(documents);
		}
		String to = event.getParameter("ch.elexis.core.mail.ui.sendMail.to");
		if (to != null) {
			sendMailDialog.setTo(to);
		}
		String subject = event.getParameter("ch.elexis.core.mail.ui.sendMail.subject");
		if (subject != null) {
			sendMailDialog.setSubject(subject);
		}
		String text = event.getParameter("ch.elexis.core.mail.ui.sendMail.text");
		if (text != null) {
			sendMailDialog.setText(text);
		}
		
		if (sendMailDialog.open() == Dialog.OK) {
			MailMessage message =
					new MailMessage().to(sendMailDialog.getTo()).cc(sendMailDialog.getCc())
					.subject(sendMailDialog.getSubject()).text(sendMailDialog.getText());
			message.setAttachments(sendMailDialog.getAttachmentsString());
			message.setDocuments(sendMailDialog.getDocumentsString());
			Optional<ITaskDescriptor> taskDescriptor =
				TaskUtil.createSendMailTaskDescriptor(sendMailDialog.getAccount().getId(), message);
			if (taskDescriptor.isPresent()) {
				try {
					new ProgressMonitorDialog(HandlerUtil.getActiveShell(event)).run(false, false,
						new IRunnableWithProgress() {
							
							@Override
							public void run(IProgressMonitor monitor)
								throws InvocationTargetException, InterruptedException{
								try {
									monitor.beginTask("Send Mail ...", IProgressMonitor.UNKNOWN);
									task = TaskUtil.executeTaskSync(taskDescriptor.get(), monitor);
									monitor.done();
								} catch (TaskException e) {
									MessageDialog.openError(HandlerUtil.getActiveShell(event),
										"Fehler", "Versenden konnte nicht gestartet werden.");
								}
							}
						});
				} catch (InvocationTargetException | InterruptedException e) {
					MessageDialog.openError(HandlerUtil.getActiveShell(event), "Fehler",
						"Versenden konnte nicht gestartet werden.");
				}
				if (!task.isSucceeded()) {
					String errorMessage = MailClientComponent.getLastErrorMessage();
					if (errorMessage.isEmpty()) {
						MessageDialog.openError(HandlerUtil.getActiveShell(event), "Fehler",
							"Versenden konnte nicht gestartet werden.");
					} else {
						MessageDialog.openError(HandlerUtil.getActiveShell(event), "Fehler",
							errorMessage);
					}
				} else {
					MessageDialog.openInformation(HandlerUtil.getActiveShell(event),
						"E-Mail versand", "E-Mail erfolgreich versendet.");
				}
				return task.getState() == TaskState.COMPLETED;
			}
		}
		return false;
	}
}
