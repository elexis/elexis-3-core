 
package ch.elexis.core.ui.reminder.commands;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.menu.MItem;

import jakarta.inject.Inject;

public class ToggleCompleted {

	@Inject
	private IEventBroker eventBroker;

	@Execute
	public void execute(MItem item) {
		if (item.isSelected()) {
			item.setTooltip("Nicht erledigte Pendenzen anzeigen");
			eventBroker.send("reminder/showCompleted", Boolean.TRUE);
		} else {
			item.setTooltip("Erledigte Pendenzen anzeigen");
			eventBroker.send("reminder/showCompleted", Boolean.FALSE);
		}
	}
}