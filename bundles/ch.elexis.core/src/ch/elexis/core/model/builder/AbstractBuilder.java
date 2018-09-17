package ch.elexis.core.model.builder;

import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IModelService;

public abstract class AbstractBuilder<T extends Identifiable> {

	public T object;
	public final IModelService modelService;

	public AbstractBuilder(IModelService modelService) {
		this.modelService = modelService; 
	}
	
	public T build() {
		return object;
	}

	public T buildAndSave() {
		build();
		boolean success = modelService.save(object);
		// TODO what if false?
		return object;
	}

}
