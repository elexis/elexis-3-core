package ch.elexis.core.mail.ui.handlers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.LoggerFactory;

import ch.elexis.core.mail.MailMessage;
import ch.elexis.core.mail.TaskUtil;
import ch.elexis.core.mail.ui.archive.ArchiveUtil;
import ch.elexis.core.mail.ui.client.MailClientComponent;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.tasks.model.ITaskDescriptor;

public class SendMailTaskWithProgress {

	private ITask task;

	public ITask execute(Shell activeShell, ITaskDescriptor taskDescriptor, MailMessage message) {
		try {
			new ProgressMonitorDialog(activeShell).run(true, false, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						monitor.beginTask("Send Mail ...", IProgressMonitor.UNKNOWN);
						task = TaskUtil.executeTaskSync(taskDescriptor, monitor);
						if (task.isSucceeded()) {
							OutboxUtil.getOrCreateElement(taskDescriptor, true);
							EncounterUtil.addMailToEncounter(taskDescriptor);
							ArchiveUtil.archiveAttachments(message.getAttachments());
						}
						monitor.done();
					} catch (TaskException e) {
						LoggerFactory.getLogger(getClass()).error("Error executing send mail task", e);
						MessageDialog.openError(activeShell, "Fehler", "Versenden konnte nicht gestartet werden.");
					}
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			LoggerFactory.getLogger(getClass()).error("Error executing send mail command", e);
			MessageDialog.openError(activeShell, "Fehler", "Versenden konnte nicht gestartet werden.");
		}
		if (!task.isSucceeded()) {
			String errorMessage = MailClientComponent.getLastErrorMessage();
			if (errorMessage.isEmpty()) {
				MessageDialog.openError(activeShell, "Fehler", "Versenden konnte nicht gestartet werden.");
			} else {
				MessageDialog.openError(activeShell, "Fehler", errorMessage);
			}
		} else {
			MessageDialog.openInformation(activeShell, "E-Mail versand", "E-Mail erfolgreich versendet.");
		}
		return task;
	}

}
