package ch.elexis.core.services.holder;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.services.IModelService;

public class CoreModelServiceHolder {

	public static IModelService get() {
		return PortableServiceLoader.getCoreModelService();
	}
}
