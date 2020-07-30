package ch.elexis.core.mail.ui.handlers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.mail.TaskUtil;
import ch.elexis.core.mail.ui.client.MailClientComponent;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.tasks.model.ITaskDescriptor;

public class SendMailTaskWithProgress {
	
	private ITask task;
	
	public ITask execute(Shell activeShell, ITaskDescriptor taskDescriptor){
		try {
			new ProgressMonitorDialog(activeShell).run(false, false,
				new IRunnableWithProgress() {
					
					@Override
					public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException{
						try {
							monitor.beginTask("Send Mail ...", IProgressMonitor.UNKNOWN);
							task = TaskUtil.executeTaskSync(taskDescriptor, monitor);
							monitor.done();
						} catch (TaskException e) {
							MessageDialog.openError(activeShell, "Fehler",
								"Versenden konnte nicht gestartet werden.");
						}
					}
				});
		} catch (InvocationTargetException | InterruptedException e) {
			MessageDialog.openError(activeShell, "Fehler",
				"Versenden konnte nicht gestartet werden.");
		}
		if (!task.isSucceeded()) {
			String errorMessage = MailClientComponent.getLastErrorMessage();
			if (errorMessage.isEmpty()) {
				MessageDialog.openError(activeShell, "Fehler",
					"Versenden konnte nicht gestartet werden.");
			} else {
				MessageDialog.openError(activeShell, "Fehler", errorMessage);
			}
		} else {
			MessageDialog.openInformation(activeShell, "E-Mail versand",
				"E-Mail erfolgreich versendet.");
		}
		return task;
	}
	
}
