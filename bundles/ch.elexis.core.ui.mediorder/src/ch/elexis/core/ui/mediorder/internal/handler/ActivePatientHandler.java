package ch.elexis.core.ui.mediorder.internal.handler;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MItem;

import ch.elexis.core.ui.mediorder.MediorderPart;

public class ActivePatientHandler {

	@Execute
	public void execute(MPart part, MItem item) {
		MediorderPart mediOrderPart = (MediorderPart) part.getObject();
		mediOrderPart.setActivePatient(item.isSelected());
	}
}
