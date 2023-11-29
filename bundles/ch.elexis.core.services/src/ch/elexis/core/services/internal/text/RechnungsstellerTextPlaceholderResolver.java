package ch.elexis.core.services.internal.text;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;

import ch.elexis.core.interfaces.ILocalizedEnum;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.format.AddressFormatUtil;
import ch.elexis.core.model.format.PersonFormatUtil;
import ch.elexis.core.services.IContext;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.text.ITextPlaceholderResolver;
import ch.elexis.core.text.PlaceholderAttribute;

@Component
public class RechnungsstellerTextPlaceholderResolver implements ITextPlaceholderResolver {

	@Override
	public String getSupportedType() {
		return "Rechnungssteller";
	}

	@Override
	public List<PlaceholderAttribute> getSupportedAttributes() {
		return Arrays.asList(RechnungsstellerAttribute.values()).stream()
				.map(m -> new PlaceholderAttribute(getSupportedType(), m.name(), m.getLocaleText()))
				.collect(Collectors.toList());
	}

	@Override
	public Optional<String> replaceByTypeAndAttribute(IContext context, String attribute) {
		IContact biller = (IContact) getIdentifiable(context).orElse(null);
		if (biller != null) {
			return Optional.ofNullable(replace(biller, attribute.toLowerCase()));
		}
		return Optional.empty();
	}

	@Override
	public Optional<? extends Identifiable> getIdentifiable(IContext context) {
		IMandator mandantor = context.getTyped(IMandator.class).orElse(null);
		if (mandantor != null) {
			return Optional.of(mandantor.getBiller());
		}
		return Optional.empty();
	}

	private String replace(IContact iContact, String lcAttribute) {

		RechnungsstellerAttribute mandantAttribut = searchEnum(RechnungsstellerAttribute.class, lcAttribute);
		switch (mandantAttribut) {
		case Anrede:
			if (iContact.isPerson()) {
				return PersonFormatUtil
						.getSalutation(CoreModelServiceHolder.get().load(iContact.getId(), IPerson.class).get());
			} else {
				return StringUtils.EMPTY;
			}
		case Name:
			if (iContact.isPerson()) {
				return iContact.getDescription2() + StringUtils.SPACE + iContact.getDescription1();
			} else {
				return iContact.getDescription1();
			}
		case Anschrift:
			return AddressFormatUtil.getPostalAddress(iContact, true);
		case Anschriftzeile:
			return AddressFormatUtil.getPostalAddress(iContact, false);
		default:
			return null;
		}
	}

	private enum RechnungsstellerAttribute implements ILocalizedEnum {
		Name("Name des Rechnungssteller"), Anrede("Anrede des Rechnungssteller"), Anschrift("Mehrzeilige Anschrift"),
		Anschriftzeile("Einzeilige Anschrift");

		final String description;

		private RechnungsstellerAttribute(String description) {
			this.description = description;
		}

		@Override
		public String getLocaleText() {
			return description;
		}
	}

}
