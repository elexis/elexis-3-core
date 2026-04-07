package ch.elexis.core.findings.util;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.findings.IFindingsService;

public class FindingsServiceHolder {

	public static IFindingsService getiFindingsService() {
		return PortableServiceLoader.get(IFindingsService.class);
	}
}
