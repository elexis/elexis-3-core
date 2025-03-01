 
package ch.elexis.core.ui.reminder.commands;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

public class CloseReminder {

	@Execute
	public void execute() {
		ContextServiceHolder.get().getTyped(IReminder.class).ifPresent(r -> {
			r.setStatus(ProcessStatus.CLOSED);
			CoreModelServiceHolder.get().save(r);
			ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, r);
		});
	}

	@CanExecute
	public boolean canExecute() {
		return ContextServiceHolder.get().getTyped(IReminder.class).isPresent();
	}
		
}