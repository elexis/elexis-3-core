package ch.elexis.core.model.builder;

import java.time.LocalDateTime;

import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.services.IModelService;

public class IEncounterBuilder extends AbstractBuilder<IEncounter> {

	/**
	 * Create an {@link IEncounter} with the provided parameters, and
	 * {@link IEncounter#getDate()} now.
	 *
	 * @param modelService
	 * @param coverage
	 * @param mandator
	 */
	public IEncounterBuilder(IModelService modelService, ICoverage coverage, IMandator mandator) {
		super(modelService);

		object = modelService.create(IEncounter.class);
		object.setCoverage(coverage);
		object.setMandator(mandator);
		object.setTimeStamp(LocalDateTime.now());
	}

	public IEncounterBuilder date(LocalDateTime date) {
		object.setTimeStamp(date);
		return this;
	}
}
