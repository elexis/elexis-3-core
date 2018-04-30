package ch.elexis.core.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.swt.program.Program;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.data.Prescription;

public class OpenCompendiumHandler extends AbstractHandler implements IHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		// get actual fix medication of the patient
		Prescription medication =
			(Prescription) ElexisEventDispatcher.getSelected(Prescription.class);
		if (medication != null) {
			String gtin = medication.getArtikel().getGTIN();
			if (gtin == null || gtin.isEmpty()) {
				gtin = medication.getArtikel().getEAN();
			}
			if (gtin != null && !gtin.isEmpty()) {
				String url =
					"http://www.compendium.ch/prod/gtin/" + medication.getArtikel().getEAN(); //$NON-NLS-1$
				Program.launch(url);
				return null;
			} else {
				// https://compendium.ch/search/BEXIN
				String url =
					"http://www.compendium.ch/search/" + medication.getArtikel().getName() + "/de"; //$NON-NLS-1$
				Program.launch(url);
				return null;
			}
		}
		String url = "http://www.compendium.ch/search/de"; //$NON-NLS-1$
		Program.launch(url);
		return null;
	}
}
