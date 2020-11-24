package ch.elexis.core.spotlight.ui.internal;

import javax.inject.Inject;

import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.spotlight.ISpotlightResultEntry;
import ch.elexis.core.spotlight.ISpotlightResultEntry.Category;

public class SpotlightUiUtil {
	
	@Inject
	private IContextService contextService;
	
	public void handleEnter(ISpotlightResultEntry selected){
		if (selected == null) {
			return;
		}
		
		Category category = selected.getCategory();
		switch (category) {
		case PATIENT:
			String patientId = selected.getLoaderString();
			IPatient patient =
				CoreModelServiceHolder.get().load(patientId, IPatient.class).orElse(null);
			contextService.setActivePatient(patient);
			break;
		
		default:
			break;
		}
	}
	
}
