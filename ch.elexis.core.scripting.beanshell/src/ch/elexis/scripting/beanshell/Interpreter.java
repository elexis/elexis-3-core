package ch.elexis.scripting.beanshell;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;

import bsh.EvalError;
import bsh.ParseException;
import bsh.TargetError;
import ch.elexis.core.data.interfaces.events.MessageEvent;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.data.Script;
import ch.rgw.tools.ExHandler;

public class Interpreter implements ch.elexis.core.data.interfaces.scripting.Interpreter,
		IExecutableExtension {
	bsh.Interpreter scripter = new bsh.Interpreter();
	
	public Interpreter(){
		
	}
	
	@Override
	public void setValue(String name, Object value) throws ElexisException{
		try {
			scripter.set(name, value);
		} catch (EvalError e) {
			ExHandler.handle(e);
			throw new ElexisException(getClass(), e.getMessage(),
				ElexisException.EE_UNEXPECTED_RESPONSE);
		}
		
	}
	
	@Override
	public Object run(String script, boolean showErrors) throws ElexisException{
		try {
			
			return scripter.eval(script);
		} catch (TargetError e) {
			if (showErrors) {
				MessageEvent.fireError("Script target Error", "Target Error: " + e.getTarget());
			}
			throw (new ElexisException(Script.class, e.getMessage(),
				ElexisException.EE_UNEXPECTED_RESPONSE));
		} catch (ParseException e) {
			String msg = "";
			if (e != null) {
				try {
					msg = e.getMessage();
					// msg = e.getErrorText();
					if (msg == null) {
						msg = "";
					}
				} catch (Exception ex) {
					ExHandler.handle(ex);
					msg = "unbekannter Fehler";
				}
			}
			if (showErrors) {
				String line = "Script Syntax Fehler " + msg;
				String titel = "Script syntax Error";
				MessageEvent.fireError(titel, line);
			}
			throw (new ElexisException(getClass(), e.getMessage(),
				ElexisException.EE_UNEXPECTED_RESPONSE));
		} catch (EvalError e) {
			if (showErrors) {
				MessageEvent.fireError("Script general error",
					"Allgemeiner Script Fehler: " + e.getErrorText());
			}
			throw (new ElexisException(Script.class, "Allgemeiner Script Fehler: "
				+ e.getErrorText(), ElexisException.EE_UNEXPECTED_RESPONSE));
		}
		
	}
	
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
		throws CoreException{
		// TODO Auto-generated method stub
		
	}
	
}
