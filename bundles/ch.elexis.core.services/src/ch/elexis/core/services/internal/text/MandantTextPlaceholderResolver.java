package ch.elexis.core.services.internal.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.constants.XidConstants;
import ch.elexis.core.interfaces.ILocalizedEnum;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IXid;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.format.PersonFormatUtil;
import ch.elexis.core.services.IContext;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.text.ITextPlaceholderResolver;
import ch.elexis.core.text.PlaceholderAttribute;

@Component
public class MandantTextPlaceholderResolver implements ITextPlaceholderResolver {

	@Reference(target = "(type=Kontakt)")
	private ITextPlaceholderResolver contactTextPlaceholderResolver;

	@Override
	public String getSupportedType() {
		return "Mandant";
	}

	@Override
	public List<PlaceholderAttribute> getSupportedAttributes() {
		List<PlaceholderAttribute> ret = new ArrayList<>();
		ret.addAll(Arrays.asList(MandantAttribute.values()).stream()
				.map(m -> new PlaceholderAttribute(getSupportedType(), m.name(), m.getLocaleText()))
				.collect(Collectors.toList()));
		if (contactTextPlaceholderResolver != null) {
			ret.addAll(contactTextPlaceholderResolver.getSupportedAttributes());
		}
		return ret;
	}

	@Override
	public Optional<String> replaceByTypeAndAttribute(IContext context, String attribute) {
		IMandator mandantor = (IMandator) getIdentifiable(context).orElse(null);
		if (mandantor != null) {
			return Optional.ofNullable(replace(mandantor, attribute.toLowerCase()));
		}
		return Optional.empty();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Optional<? extends Identifiable> getIdentifiable(IContext context) {
		Optional<IMandator> ret = context.getTyped(IMandator.class);
		if (ret.isEmpty()) {
			ret = (Optional<IMandator>) context.getNamed(getSupportedType());
		}
		return ret;
	}

	private String replace(IMandator mandator, String lcAttribute) {

		MandantAttribute mandantAttribut = searchEnum(MandantAttribute.class, lcAttribute);
		if (mandantAttribut != null) {
			switch (mandantAttribut) {
			case Anrede:
				if (mandator.isPerson()) {
					return PersonFormatUtil
							.getSalutation(CoreModelServiceHolder.get().load(mandator.getId(), IPerson.class).get());
				} else {
					return StringUtils.EMPTY;
				}
			case Name:
				return mandator.getDescription1();
			case Vorname:
				return mandator.getDescription2();
			case Titel:
				if (mandator.isPerson()) {
					return CoreModelServiceHolder.get().load(mandator.getId(), IPerson.class).get().getTitel();
				}
			case TarmedSpezialität:
				return (String) mandator.getExtInfo("TarmedSpezialität");
			case EAN:
				IXid xid = mandator.getXid(XidConstants.EAN);
				if (xid != null) {
					return xid.getDomainId();
				}
			case KSK:
				xid = mandator.getXid(XidConstants.DOMAIN_KSK);
				if (xid != null) {
					return xid.getDomainId();
				}
			case Spezialität:
				return (String) mandator.getExtInfo("Spezialität");
			default:
				break;
			}
		}
		// fallback to contact properties
		if (contactTextPlaceholderResolver != null) {
			IContact contact = CoreModelServiceHolder.get().load(mandator.getId(), IContact.class).get();
			TextPlaceholderContext context = new TextPlaceholderContext(contact);
			Optional<String> contactReplacement = contactTextPlaceholderResolver.replaceByTypeAndAttribute(context,
					lcAttribute);
			if (contactReplacement.isPresent()) {
				return contactReplacement.get();
			}
		}
		return null;
	}

	private enum MandantAttribute implements ILocalizedEnum {
		Name("Nachname des Mandanten"), Vorname("Vorname des Mandanten"), Titel("Titel des Mandanten"),
		Anrede("Anrede des Mandanten"), TarmedSpezialität("Tarmed Spezialität des Mandanten"), EAN("EAN des Mandanten"),
		KSK("KSK des Mandanten"), Spezialität("Spezialität des Mandanten");

		final String description;

		private MandantAttribute(String description) {
			this.description = description;
		}

		@Override
		public String getLocaleText() {
			return description;
		}
	}

}
