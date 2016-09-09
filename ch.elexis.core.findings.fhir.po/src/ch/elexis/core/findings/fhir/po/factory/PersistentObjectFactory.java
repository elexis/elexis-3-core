package ch.elexis.core.findings.fhir.po.factory;

import java.lang.reflect.Method;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.data.PersistentObject;

public class PersistentObjectFactory extends ch.elexis.data.PersistentObjectFactory {
	
	@SuppressWarnings({
		"unchecked", "rawtypes"
	})
	public PersistentObject createFromString(String code){
		try {
			String[] ci = code.split(StringConstants.DOUBLECOLON);
			if (!ci[0].startsWith("ch.elexis.core.findings.fhir.po")) { //$NON-NLS-1$
				return null;
			}
			Class clazz = Class.forName(ci[0]);
			Method load = clazz.getMethod("load", new Class[] { //$NON-NLS-0$
				String.class
			});
			return (PersistentObject) (load.invoke(null, new Object[] {
				ci[1]
			}));
		} catch (Exception ex) {
			// ExHandler.handle(ex);
			return null;
		}
	}
	
	@Override
	public PersistentObject doCreateTemplate(Class<? extends PersistentObject> typ){
		try {
			return (PersistentObject) typ.newInstance();
		} catch (Exception ex) {
			// ExHandler.handle(ex);
			return null;
		}
	}
	
}
