 
package ch.elexis.core.ui.reminder.commands;

import java.text.MessageFormat;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

public class DeleteReminder {

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		ContextServiceHolder.get().getTyped(IReminder.class).ifPresent(r -> {
			Display.getDefault().syncExec(() -> {
				if (MessageDialog.openQuestion(shell, Messages.Core_Really_delete_caption,
						MessageFormat.format(Messages.Core_Really_delete_0, "Pendenz '" + r.getSubject() + "'"))) {
					CoreModelServiceHolder.get().delete(r);
				}
			});
		});
	}

	@CanExecute
	public boolean canExecute() {
		return ContextServiceHolder.get().getTyped(IReminder.class).isPresent();
	}
		
}