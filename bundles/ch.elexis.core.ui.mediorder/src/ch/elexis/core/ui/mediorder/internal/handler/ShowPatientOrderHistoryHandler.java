package ch.elexis.core.ui.mediorder.internal.handler;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledToolItem;

import ch.elexis.core.ui.mediorder.MediorderPart;

public class ShowPatientOrderHistoryHandler {

	@Execute
	public void execute(MPart part, MHandledToolItem item) {
		if (part == null || !(part.getObject() instanceof MediorderPart)) {
			return;
		}
		MediorderPart mediorderPart = (MediorderPart) part.getObject();
		item.setSelected(!mediorderPart.toggleViews());
	}
}
