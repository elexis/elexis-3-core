package ch.elexis.core.eigenartikel;

import java.lang.reflect.Method;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.PersistentObjectFactory;

public class EigenartikelPersistentObjectFactory extends PersistentObjectFactory {
	
	@SuppressWarnings("deprecation")
	@Override
	public PersistentObject createFromString(String code){
		try {
			String[] ci = code.split(StringConstants.DOUBLECOLON);
			Class<?> clazz = null;
			if (ci[0].equals(Eigenartikel.class.getCanonicalName())
				|| ci[0].equals(ch.elexis.data.Eigenartikel.class.getCanonicalName())
				|| ci[0].equals(ch.elexis.eigenartikel.Eigenartikel.class.getCanonicalName())) {
				clazz = Class.forName(Eigenartikel.class.getCanonicalName());
			} else {
				clazz = Class.forName(ci[0]);
			}
			Method load = clazz.getMethod("load", new Class[] { String.class}); //$NON-NLS-1$
			return (PersistentObject) (load.invoke(null, new Object[] {
				ci[1]
			}));
		} catch (Exception ex) {
			return null;
		}
	}
	
	@Override
	public PersistentObject doCreateTemplate(Class<? extends PersistentObject> typ){
		try {
			return (PersistentObject) typ.newInstance();
		} catch (Exception ex) {
			return null;
		}
	}
	
	@Override
	public Class getClassforName(String fullyQualifiedClassName){
		Class ret = null;
		try {
			if (fullyQualifiedClassName
				.equals(ch.elexis.data.Eigenartikel.class.getCanonicalName()))
				return Class.forName(Eigenartikel.class.getCanonicalName());
			ret = Class.forName(fullyQualifiedClassName);
			return ret;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return ret;
		}
	}
}
