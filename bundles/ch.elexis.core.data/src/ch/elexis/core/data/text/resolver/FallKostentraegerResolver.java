package ch.elexis.core.data.text.resolver;

import java.util.Optional;

import ch.elexis.core.data.interfaces.text.ITextResolver;
import ch.elexis.core.data.util.FallDataAccessor;
import ch.elexis.data.Fall;
import ch.elexis.data.Kontakt;

public class FallKostentraegerResolver implements ITextResolver {

	/**
	 * @see FallDataAccessor#getObject(String, ch.elexis.data.PersistentObject, String, String[])
	 */
	@Override
	public Optional<String> resolve(Object object) {
		if (object instanceof Fall) {
			Kontakt costBearer = ((Fall) object).getCostBearer();
			if (costBearer != null) {
				String label = costBearer.getLabel();
				String fullName = label.substring(0, label.indexOf(","));
				return Optional.of(fullName);
			}
		}
		return Optional.empty();
	}

}
