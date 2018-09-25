package ch.elexis.core.model.builder;

import ch.elexis.core.model.ILabItem;
import ch.elexis.core.model.ILabResult;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IModelService;

public class ILabResultBuilder extends AbstractBuilder<ILabResult> {
	
	public ILabResultBuilder(IModelService modelService, ILabItem labItem, IPatient patient){
		super(modelService);
		object = modelService.create(ILabResult.class);
		object.setItem(labItem);
		object.setPatient(patient);
	}
	
}
