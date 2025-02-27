 
package ch.elexis.core.ui.reminder.menu;

import java.util.List;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.issue.Priority;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

public class ImportantMenuContribution {

	@AboutToShow
	public void aboutToShow(MPart mpart, List<MMenuElement> items) {
		ContextServiceHolder.get().getTyped(IReminder.class).ifPresent(r -> {
			MDirectMenuItem dynamicItem = MMenuFactory.INSTANCE.createDirectMenuItem();
			dynamicItem.setType(ItemType.CHECK);
			dynamicItem.setLabel("Wichtig");
			dynamicItem.setContributionURI("bundleclass://ch.elexis.core.ui.reminder/" + getClass().getName()); //$NON-NLS-1$
			dynamicItem.setSelected(r.getPriority() == Priority.HIGH);
			dynamicItem.setEnabled(true);
			dynamicItem.getTransientData().put("reminder", r); //$NON-NLS-1$
			items.add(dynamicItem);
		});
	}

	@Execute
	private void toggleMine(MPart mpart, MDirectMenuItem menuItem) {
		IReminder reminder = (IReminder) menuItem.getTransientData().get("reminder"); //$NON-NLS-1$

		reminder.setPriority(reminder.getPriority() == Priority.HIGH ? Priority.MEDIUM : Priority.HIGH);
		CoreModelServiceHolder.get().save(reminder);
		ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, reminder);
	}
}