package ch.elexis.core.model.builder;

import java.time.LocalDateTime;

import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IRecipe;
import ch.elexis.core.services.IModelService;

public class IRecipeBuilder extends AbstractBuilder<IRecipe> {
	
	public IRecipeBuilder(IModelService modelService, IPatient patient, IMandator mandator){
		super(modelService);
		
		object = modelService.create(IRecipe.class);
		object.setDate(LocalDateTime.now());
		object.setPatient(patient);
		object.setMandator(mandator);
	}
	
}
