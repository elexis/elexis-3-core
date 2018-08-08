package ch.elexis.core.ui.eigendiagnosen;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IModelService;

@Component
public class ModelServiceHolder {
	
	private static IModelService modelService;
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME
		+ "=ch.elexis.core.eigendiagnosen.model)")
	public void setModelServcie(IModelService modelService){
		ModelServiceHolder.modelService = modelService;
	}
	
	public static IModelService get(){
		if (modelService == null) {
			throw new IllegalStateException("No ModelService available");
		}
		return modelService;
	}
}
