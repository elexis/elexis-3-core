package ch.elexis.core.mail.ui.handlers;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.mail.MailMessage;
import ch.elexis.core.mail.TaskUtil;
import ch.elexis.core.mail.ui.dialogs.SendMailDialog;
import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.TaskState;

public class SendMailTaskHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String taskId = event.getParameter("ch.elexis.core.mail.ui.sendMailTaskDescriptorId");

		Optional<ITaskDescriptor> taskDescriptor = TaskUtil.getTaskDescriptor(taskId);
		if (taskDescriptor.isPresent()) {
			MailMessage message = MailMessage.fromJson(taskDescriptor.get().getRunContext().get("message"));
			String accountId = (String) taskDescriptor.get().getRunContext().get("accountId");
			if (message != null) {
				SendMailDialog sendMailDialog = new SendMailDialog(HandlerUtil.getActiveShell(event));
				sendMailDialog.setMailMessage(message);
				sendMailDialog.setAccountId(accountId);
				sendMailDialog.disableOutbox();
				Optional<ITask> execution = TaskServiceHolder.get().findLatestExecution(taskDescriptor.get());
				if (execution.isPresent()) {
					// setting sent will open in view mode ...
					sendMailDialog.sent(execution.get().getFinishedAt());
				}
				if (sendMailDialog.open() == Dialog.OK) {
					MailMessage sendMessage = new MailMessage().to(sendMailDialog.getTo()).cc(sendMailDialog.getCc())
							.subject(sendMailDialog.getSubject()).text(sendMailDialog.getText());
					sendMessage.setAttachments(sendMailDialog.getAttachmentsString());
					sendMessage.setDocuments(sendMailDialog.getDocumentsString());

					Map<String, Serializable> runContext = taskDescriptor.get().getRunContext();
					runContext.put("accountId", sendMailDialog.getAccount().getId());
					runContext.put("message", sendMessage);
					taskDescriptor.get().setRunContext(runContext);

					ITaskDescriptor descriptor = TaskUtil.configureTaskDescriptor(taskDescriptor.get(),
							sendMailDialog.getAccount().getId(), sendMessage);
					ITask task = new SendMailTaskWithProgress().execute(HandlerUtil.getActiveShell(event), descriptor,
							message);
					return Boolean.valueOf(task.getState() == TaskState.COMPLETED);
				}
			}
		} else {
			MessageDialog.openError(HandlerUtil.getActiveShell(event), "Fehler",
					"Der mail Task konnte nicht geöffnet werden.");
		}
		return Boolean.FALSE;
	}

}
