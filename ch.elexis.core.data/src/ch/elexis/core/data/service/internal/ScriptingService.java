package ch.elexis.core.data.service.internal;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.services.IScriptingService;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Script;

@Component
public class ScriptingService implements IScriptingService {
	
	@Override
	public Object execute(String script) throws ElexisException{
		return Script.execute(Script.getInterpreterFor(script), script, null, false,
			new PersistentObject[0]);
	}
}
