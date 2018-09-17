package ch.elexis.core.model.test;

import ch.elexis.core.services.IModelService;
import ch.elexis.core.utils.OsgiServiceUtil;

public class CoreModelServiceHolder {
	
	private static IModelService modelService;
	
	public static IModelService get(){
		if (modelService == null) {
			modelService = OsgiServiceUtil.getService(IModelService.class).get();
		}
		return modelService;
	}
}