package ch.elexis.core.data.service;

import ch.elexis.core.rcp.utils.OsgiServiceUtil;
import ch.elexis.core.services.IContextService;

public class ContextServiceHolder {

	private static IContextService contextService;

	public static synchronized IContextService get() {
		if (contextService == null) {
			contextService = OsgiServiceUtil.getServiceWait(IContextService.class, 100).orElse(null);
		}
		return contextService;
	}
}
