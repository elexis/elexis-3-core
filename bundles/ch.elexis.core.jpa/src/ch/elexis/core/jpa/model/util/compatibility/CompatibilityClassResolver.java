package ch.elexis.core.jpa.model.util.compatibility;

import java.io.ObjectStreamClass;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;

import ch.elexis.core.jpa.model.util.compatibility.Kontakt.statL;
import ch.elexis.core.model.Statistics;

public class CompatibilityClassResolver {

	public Class<?> resolveClass(ObjectStreamClass desc) throws ClassNotFoundException {
		if (desc.getName().equals("ch.elexis.data.Kontakt$statL")) { //$NON-NLS-1$
			return CompatibilityClassResolver.class.getClassLoader()
					.loadClass("ch.elexis.core.jpa.model.util.compatibility.Kontakt$statL"); //$NON-NLS-1$
		}
		if (desc.getName().equals("ch.elexis.util.MFUList")) { //$NON-NLS-1$
			return CompatibilityClassResolver.class.getClassLoader()
					.loadClass("ch.elexis.core.jpa.model.util.compatibility.MFUList"); //$NON-NLS-1$
		}
		if (desc.getName().equals("ch.elexis.util.MFUList$Entry")) { //$NON-NLS-1$
			return CompatibilityClassResolver.class.getClassLoader()
					.loadClass("ch.elexis.core.jpa.model.util.compatibility.MFUList$Entry"); //$NON-NLS-1$
		}
		return null;
	}

	public static void replaceCompatibilityObjects(Hashtable<Object, Object> readObject) {
		Collection<Object> keys = readObject.keySet();
		for (Object key : keys) {
			Object value = readObject.get(key);
			if (value instanceof List) {
				List<?> list = (List<?>) value;
				ArrayList<Object> replacementList = new ArrayList<>();
				boolean replaced = false;
				for (Object object : list) {
					Optional<Object> replacement = getReplacementObject(object);
					if (replacement.isPresent()) {
						replacementList.add(replacement.get());
						replaced = true;
					} else {
						replacementList.add(object);
					}
				}
				if (replaced) {
					readObject.put(key, replacementList);
				}
			} else {
				getReplacementObject(value).ifPresent(replacement -> readObject.put(key, replacement));
			}
		}
	}

	private static Optional<Object> getReplacementObject(Object object) {
		if (object.getClass().getName().startsWith("ch.elexis.core.jpa.model.util.compatibility")) { //$NON-NLS-1$
			if (object instanceof Kontakt.statL) {
				return Optional.of(getStatistics((Kontakt.statL) object));
			}
		}
		return Optional.empty();
	}

	private static Statistics getStatistics(statL value) {
		Statistics ret = new Statistics(value.v);
		ret.setCount(value.c);
		return ret;
	}
}
