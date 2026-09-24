package ch.elexis.core.findings.util;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.findings.codes.IValueSetService;

public class ValueSetServiceHolder {

	public static IValueSetService getIValueSetService() {
		return PortableServiceLoader.get(IValueSetService.class);
	}
}
