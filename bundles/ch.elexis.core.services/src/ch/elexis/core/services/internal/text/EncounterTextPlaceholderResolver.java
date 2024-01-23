package ch.elexis.core.services.internal.text;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.interfaces.ILocalizedEnum;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IContext;
import ch.elexis.core.text.ITextPlaceholderResolver;
import ch.elexis.core.text.PlaceholderAttribute;

@Component
public class EncounterTextPlaceholderResolver implements ITextPlaceholderResolver {

	@Override
	public String getSupportedType() {
		return "Konsultation";
	}

	@Override
	public List<PlaceholderAttribute> getSupportedAttributes() {
		return Arrays.asList(KonsultationAttribute.values()).stream()
				.map(m -> new PlaceholderAttribute(getSupportedType(), m.name(), m.getLocaleText()))
				.collect(Collectors.toList());
	}

	@Override
	public Optional<String> replaceByTypeAndAttribute(IContext context, String attribute) {
		IEncounter encounter = (IEncounter) getIdentifiable(context).orElse(null);
		if (encounter != null) {
			return Optional.ofNullable(replace(encounter, attribute.toLowerCase()));
		}
		return Optional.empty();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Optional<? extends Identifiable> getIdentifiable(IContext context) {
		Optional<IEncounter> ret = context.getTyped(IEncounter.class);
		if (ret.isEmpty()) {
			ret = (Optional<IEncounter>) context.getNamed(getSupportedType());
		}
		return ret;
	}

	private String replace(IEncounter encounter, String lcAttribute) {
		KonsultationAttribute attribute = searchEnum(KonsultationAttribute.class, lcAttribute);
		switch (attribute) {
		case Eintrag:
			return encounter.getHeadVersionInPlaintext();
		default:
			return null;
		}
	}

	private enum KonsultationAttribute implements ILocalizedEnum {
		Eintrag("Text der Konsultation");

		final String description;

		private KonsultationAttribute(String description) {
			this.description = description;
		}

		@Override
		public String getLocaleText() {
			return description;
		}
	}

}
