package ch.elexis.core.findings.util;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IScriptingService;

@Component(service = {})
public class ScriptingServiceHolder {
	
	private static IScriptingService iScriptingService;
	
	@Reference(unbind = "-")
	public static void setScriptingService(IScriptingService iScriptingService){
		ScriptingServiceHolder.iScriptingService = iScriptingService;
	}
	
	public static IScriptingService getService(){
		return iScriptingService;
	}
}
