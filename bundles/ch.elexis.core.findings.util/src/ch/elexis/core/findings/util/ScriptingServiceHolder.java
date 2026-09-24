package ch.elexis.core.findings.util;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.services.IScriptingService;

public class ScriptingServiceHolder {

	public static IScriptingService getService() {
		return PortableServiceLoader.get(IScriptingService.class);
	}
}
