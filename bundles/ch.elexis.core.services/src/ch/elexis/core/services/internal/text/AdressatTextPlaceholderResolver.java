package ch.elexis.core.services.internal.text;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;

import ch.elexis.core.interfaces.ILocalizedEnum;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.format.AddressFormatUtil;
import ch.elexis.core.model.format.PersonFormatUtil;
import ch.elexis.core.services.IContext;
import ch.elexis.core.text.ITextPlaceholderResolver;
import ch.elexis.core.text.PlaceholderAttribute;

@Component
public class AdressatTextPlaceholderResolver implements ITextPlaceholderResolver {

	@Override
	public String getSupportedType() {
		return "Adressat";
	}

	@Override
	public List<PlaceholderAttribute> getSupportedAttributes() {
		return Arrays.asList(AdressatAttribute.values()).stream()
				.map(m -> new PlaceholderAttribute(getSupportedType(), m.name(), m.getLocaleText()))
				.collect(Collectors.toList());
	}

	@Override
	public Optional<String> replaceByTypeAndAttribute(IContext context, String attribute) {
		IContact adressat = (IContact) getIdentifiable(context).orElse(null);
		if (adressat != null) {
			return Optional.ofNullable(replace(adressat, attribute.toLowerCase()));
		}
		return Optional.empty();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Optional<? extends Identifiable> getIdentifiable(IContext context) {
		return ((Optional<IContact>) context.getNamed(getSupportedType()));
	}

	private String replace(IContact addressat, String lcAttribute) {

		AdressatAttribute patientAttribut = searchEnum(AdressatAttribute.class, lcAttribute);
		if (patientAttribut != null) {
			switch (patientAttribut) {
			case Anrede:
				if (addressat.isPerson()) {
					return PersonFormatUtil.getSalutation(addressat.asIPerson());
				} else {
					return StringUtils.EMPTY;
				}
			case Name:
				if (addressat.isPerson()) {
					return addressat.asIPerson().getLastName();
				} else {
					return addressat.getDescription1();
				}
			case Vorname:
				if (addressat.isPerson()) {
					return addressat.asIPerson().getFirstName();
				} else {
					return StringUtils.EMPTY;
				}
			case Anschrift:
				return AddressFormatUtil.getPostalAddress(addressat, true);
			case Anschriftzeile:
				return AddressFormatUtil.getPostalAddress(addressat, false);
			}
		}
		return null;
	}

	private enum AdressatAttribute implements ILocalizedEnum {
		Name("Nachname des Adressat"), Vorname("Vorname des Adressat"), Anrede("Anrede des Adressat"),
		Anschrift("Mehrzeilige Anschrift"), Anschriftzeile("Einzeilige Anschrift");

		final String description;

		private AdressatAttribute(String description) {
			this.description = description;
		}

		@Override
		public String getLocaleText() {
			return description;
		}
	}

}
