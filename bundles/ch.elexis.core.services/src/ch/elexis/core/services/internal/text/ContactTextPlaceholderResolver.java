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
import ch.elexis.core.services.IContext;
import ch.elexis.core.text.ITextPlaceholderResolver;
import ch.elexis.core.text.PlaceholderAttribute;

@Component(property = { "type=Kontakt" })
public class ContactTextPlaceholderResolver implements ITextPlaceholderResolver {

	@Override
	public String getSupportedType() {
		return "Kontakt";
	}

	@Override
	public List<PlaceholderAttribute> getSupportedAttributes() {
		return Arrays.asList(ContactAttribute.values()).stream()
				.map(m -> new PlaceholderAttribute(getSupportedType(), m.name(), m.getLocaleText()))
				.collect(Collectors.toList());
	}

	@Override
	public Optional<String> replaceByTypeAndAttribute(IContext context, String attribute) {
		IContact contact = (IContact) getIdentifiable(context).orElse(null);
		if (contact != null) {
			return Optional.ofNullable(replace(contact, attribute.toLowerCase()));
		}
		return Optional.empty();
	}

	@Override
	public Optional<? extends Identifiable> getIdentifiable(IContext context) {
		return context.getTyped(IContact.class);
	}

	private String replace(IContact contact, String lcAttribute) {

		ContactAttribute contactAttribut = searchEnum(ContactAttribute.class, lcAttribute);
		if (contactAttribut != null) {
			switch (contactAttribut) {
			case Name:
				StringBuilder sb = new StringBuilder();
				sb.append(contact.getDescription1()).append(StringUtils.SPACE)
						.append(StringUtils.defaultString(contact.getDescription2()));
				if (!StringUtils.isBlank(contact.getDescription3())) {
					sb.append("(").append(contact.getDescription3()).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			case Anschrift:
				return AddressFormatUtil.getPostalAddress(contact, true);
			case Anschriftzeile:
				return AddressFormatUtil.getPostalAddress(contact, false);
			case Strasse:
				return StringUtils.defaultString(contact.getStreet());
			case Plz:
				return StringUtils.defaultString(contact.getZip());
			case Ort:
				return StringUtils.defaultString(contact.getCity());
			case Telefon1:
				return StringUtils.defaultString(contact.getPhone1());
			case Telefon2:
				return StringUtils.defaultString(contact.getPhone2());
			case Natel:
				return StringUtils.defaultString(contact.getMobile());
			case E_Mail:
				return StringUtils.defaultString(contact.getEmail());
			default:
				break;
			}
		}
		return null;
	}

	private enum ContactAttribute implements ILocalizedEnum {
		Name("Name des Kontakt"), Anschrift("Mehrzeilige Anschrift"), Anschriftzeile("Einzeilige Anschrift"),
		Strasse("Strasse des Kontakt"), Plz("Postleitzahl des Kontakt"), Ort("Ort des Kontakt"),
		Telefon1("Telefon des Kontakt"), Natel("Natel des Kontakt"), Telefon2("2tes Telefon des Kontakt"),
		E_Mail("E-Mail des Kontakt");

		final String description;

		private ContactAttribute(String description) {
			this.description = description;
		}

		@Override
		public String getLocaleText() {
			return description;
		}
	}

}
