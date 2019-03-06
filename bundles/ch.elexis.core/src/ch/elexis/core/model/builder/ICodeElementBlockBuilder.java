package ch.elexis.core.model.builder;

import ch.elexis.core.model.ICodeElementBlock;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.services.IModelService;

public class ICodeElementBlockBuilder extends AbstractBuilder<ICodeElementBlock> {
	
	/**
	 * Create an {@link ICodeElementBlock} with the provided parameters.
	 * 
	 * @param modelService
	 * @param code
	 */
	public ICodeElementBlockBuilder(IModelService modelService, String code){
		super(modelService);
		
		object = modelService.create(ICodeElementBlock.class);
		object.setCode(code);
	}
	
	public ICodeElementBlockBuilder mandator(IMandator mandator){
		object.setMandator(mandator);
		return this;
	}
	
	public ICodeElementBlockBuilder macro(String macro){
		object.setMacro(macro);
		return this;
	}
	
}
