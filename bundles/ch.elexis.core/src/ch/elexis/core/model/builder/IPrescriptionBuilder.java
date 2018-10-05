package ch.elexis.core.model.builder;

import java.time.LocalDateTime;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.IModelService;

public class IPrescriptionBuilder extends AbstractBuilder<IPrescription> {
	
	public IPrescriptionBuilder(IModelService modelService, IArticle article, IPatient patient,
		String dosageInstruction){
		super(modelService);
		
		object = modelService.create(IPrescription.class);
		object.setDateFrom(LocalDateTime.now());
		object.setPatient(patient);
		object.setArticle(article);
		object.setDosageInstruction(dosageInstruction);
		object.setEntryType(EntryType.FIXED_MEDICATION);
	}
	
}
