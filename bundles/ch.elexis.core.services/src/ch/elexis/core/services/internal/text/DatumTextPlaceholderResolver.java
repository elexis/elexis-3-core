package ch.elexis.core.services.internal.text;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.interfaces.ILocalizedEnum;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IContext;
import ch.elexis.core.text.ITextPlaceholderResolver;
import ch.elexis.core.text.PlaceholderAttribute;
import ch.elexis.core.time.TimeUtil;

@Component
public class DatumTextPlaceholderResolver implements ITextPlaceholderResolver {

	@Override
	public String getSupportedType() {
		return "Datum";
	}

	@Override
	public List<PlaceholderAttribute> getSupportedAttributes() {
		return Arrays.asList(DatumAttribute.values()).stream()
				.map(m -> new PlaceholderAttribute(getSupportedType(), m.name(), m.getLocaleText()))
				.collect(Collectors.toList());
	}

	@Override
	public Optional<String> replaceByTypeAndAttribute(IContext context, String attribute) {
		return Optional.ofNullable(replace(attribute.toLowerCase()));
	}

	@Override
	public Optional<? extends Identifiable> getIdentifiable(IContext context) {
		return Optional.empty();
	}

	private String replace(String lcAttribute) {

		DatumAttribute datumAttribut = searchEnum(DatumAttribute.class, lcAttribute);
		if (datumAttribut != null) {
			switch (datumAttribut) {
			case Heute:
				return TimeUtil.DATE_GER.format(LocalDate.now());
			}
		}
		return null;
	}

	private enum DatumAttribute implements ILocalizedEnum, IPlaceholderAttributeEnum {
		Heute("Datum Heute", new String[] { "Datum" });

		final String description;
		private String[] alternativeNames;

		private DatumAttribute(String description) {
			this.description = description;
		}

		private DatumAttribute(String description, String[] alternativeNames) {
			this.description = description;
			this.alternativeNames = alternativeNames;
		}

		@Override
		public String getLocaleText() {
			return description;
		}

		@Override
		public List<String> getAlternativeNames() {
			return alternativeNames != null ? Arrays.asList(alternativeNames) : Collections.emptyList();
		}
	}
}
