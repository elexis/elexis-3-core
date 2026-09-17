package ch.elexis.core.ui.mediorder.internal.handler;

import java.util.List;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;

import ch.elexis.core.model.IPatient;
import ch.elexis.core.ui.mediorder.MediorderPart;

public class DeleteImportedPatientHandler {

	@Execute
	public void execute(MPart part) {
		MediorderPart mediOrderPart = (MediorderPart) part.getObject();
		List<IPatient> selectedImportedPatients = mediOrderPart.getSelectedImportedPatients();
		for (IPatient patient : selectedImportedPatients) {
			mediOrderPart.deleteImportedPatient(patient);
		}
		mediOrderPart.refresh();
	}
}
