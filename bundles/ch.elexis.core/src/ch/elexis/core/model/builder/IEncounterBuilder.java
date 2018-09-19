package ch.elexis.core.model.builder;

import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.services.IModelService;

public class IEncounterBuilder extends AbstractBuilder<IEncounter> {


	public IEncounterBuilder(IModelService modelService, ICoverage coverage, IMandator mandator){
		super(modelService);
		
		object = modelService.create(IEncounter.class);
		object.setCoverage(coverage);
		object.setMandator(mandator);
	}
	
}
