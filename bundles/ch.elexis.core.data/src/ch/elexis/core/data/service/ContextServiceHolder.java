package ch.elexis.core.data.service;

import ch.elexis.core.services.IContextService;
import ch.elexis.core.utils.OsgiServiceUtil;

public class ContextServiceHolder {

	private static IContextService contextService;

	public static synchronized IContextService get() {
		if (contextService == null) {
			contextService = OsgiServiceUtil.getServiceWait(IContextService.class, 100).orElse(null);
		}
		return contextService;
	}
}
