package ch.elexis.core.data.text.resolver;

import java.util.Optional;

import ch.elexis.core.data.interfaces.text.ITextResolver;
import ch.elexis.data.Fall;
import ch.elexis.data.Kontakt;

public class FallKostentraegerResolver implements ITextResolver {

	@Override
	public Optional<String> resolve(Object object) {
		if (object instanceof Fall) {
			Kontakt costBearer = ((Fall) object).getCostBearer();
			if (costBearer != null) {
				return Optional.of(costBearer.getLabel());
			}
		}
		return Optional.empty();
	}

}
