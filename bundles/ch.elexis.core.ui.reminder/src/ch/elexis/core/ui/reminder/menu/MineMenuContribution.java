 
package ch.elexis.core.ui.reminder.menu;

import java.util.List;
import java.util.Optional;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

public class MineMenuContribution {

	@AboutToShow
	public void aboutToShow(MPart mpart, List<MMenuElement> items) {
		ContextServiceHolder.get().getTyped(IReminder.class).ifPresent(r -> {
			MDirectMenuItem dynamicItem = MMenuFactory.INSTANCE.createDirectMenuItem();
			dynamicItem.setType(ItemType.CHECK);
			dynamicItem.setLabel(Messages.Reminders_AssignedToMe);
			dynamicItem.setContributionURI("bundleclass://ch.elexis.core.ui.reminder/" + getClass().getName()); //$NON-NLS-1$
			dynamicItem.setSelected(isMine(r));
			dynamicItem.setEnabled(!isMine(r));
			dynamicItem.getTransientData().put("reminder", r); //$NON-NLS-1$
			items.add(dynamicItem);
		});
	}

	private boolean isMine(IReminder r) {
		Optional<IContact> activeUserContact = ContextServiceHolder.get().getActiveUserContact();
		if(activeUserContact.isPresent()) {
			return r.getResponsible() != null && r.getResponsible().contains(activeUserContact.get());
		}
		return false;
	}

	@Execute
	private void toggleMine(MPart mpart, MDirectMenuItem menuItem) {
		IReminder reminder = (IReminder) menuItem.getTransientData().get("reminder"); //$NON-NLS-1$
		Optional<IContact> activeUserContact = ContextServiceHolder.get().getActiveUserContact();
		if (activeUserContact.isPresent()) {
			reminder.setGroup(null);
			reminder.getResponsible().forEach(c -> {
				reminder.removeResponsible(c);
			});
			reminder.setResponsibleAll(false);
			reminder.addResponsible(activeUserContact.get());
			CoreModelServiceHolder.get().save(reminder);
			ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, reminder);
		}
	}
}