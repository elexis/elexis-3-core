package ch.elexis.core.model.builder;

import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IModelService;

public abstract class AbstractBuilder<T extends Identifiable> {
	
	public T object;
	public final IModelService modelService;
	
	public AbstractBuilder(IModelService modelService){
		this.modelService = modelService;
	}
	
	/**
	 * Build the requested object without persisting it. <br>
	 * <b>Warning</b> Using this method with chained methods that instantiate third party objects
	 * will NOT store these created objects! Builders in this state should throw
	 * {@link IllegalStateException}
	 * 
	 * @return
	 */
	public T build(){
		return object;
	}
	
	public T buildAndSave(){
		build();
		boolean success = modelService.save(object);
		// TODO what if false?
		return object;
	}
	
}
