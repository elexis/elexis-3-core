package ch.elexis.core.services.internal.text;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.interfaces.ILocalizedEnum;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.format.PostalAddress;
import ch.elexis.core.services.IContext;
import ch.elexis.core.services.ICoverageService;
import ch.elexis.core.text.ITextPlaceholderResolver;
import ch.elexis.core.text.PlaceholderAttribute;

@Component
public class CoverageTextPlaceholderResolver implements ITextPlaceholderResolver {

	@Reference
	private ICoverageService coverageService;

	@Override
	public String getSupportedType() {
		return "Fall";
	}

	@Override
	public List<PlaceholderAttribute> getSupportedAttributes() {
		return Arrays.asList(FallAttribute.values()).stream()
				.map(m -> new PlaceholderAttribute(getSupportedType(), m.name(), m.getLocaleText()))
				.collect(Collectors.toList());
	}

	@Override
	public Optional<String> replaceByTypeAndAttribute(IContext context, String attribute) {
		ICoverage coverage = (ICoverage) getIdentifiable(context).orElse(null);
		if (coverage != null) {
			return Optional.ofNullable(replace(coverage, attribute.toLowerCase()));
		}
		return Optional.empty();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Optional<? extends Identifiable> getIdentifiable(IContext context) {
		Optional<ICoverage> ret = context.getTyped(ICoverage.class);
		if (ret.isEmpty()) {
			ret = (Optional<ICoverage>) context.getNamed(getSupportedType());
		}
		return ret;
	}

	private String replace(ICoverage coverage, String lcAttribute) {
		FallAttribute attribute = searchEnum(FallAttribute.class, lcAttribute);
		IContact contact = null;
		switch (attribute) {
		case Versicherungsnummer:
			return (String) coverage.getInsuranceNumber();
		case Kostentraeger:
			return coverage.getCostBearer() != null ? coverage.getCostBearer().getPostalAddress() : StringUtils.EMPTY;
		case KostentraegerKuerzel:
			if (coverage.getCostBearer() != null) {
				String label = coverage.getCostBearer().getLabel();
				if (label.lastIndexOf(",") > 0) {
					String fullName = label.substring(0, label.indexOf(","));
					return fullName;
				}
			}
			return StringUtils.EMPTY;
		case KostentraegerOrt:
			if (coverage.getCostBearer() != null) {
				return PostalAddress.of(coverage.getCostBearer()).getCity();
			}
			return StringUtils.EMPTY;
		case Arbeitgeber:
			contact = coverageService.getRequiredContact(coverage, "Arbeitgeber");
			if (contact != null) {
				return contact.getPostalAddress();
			}
			return StringUtils.EMPTY;
		case ArbeitgeberKuerzel:
			contact = coverageService.getRequiredContact(coverage, "Arbeitgeber");
			if (contact != null) {
				String label = contact.getLabel();
				if (label.lastIndexOf(",") > 0) {
					String fullName = label.substring(0, label.indexOf(","));
					return fullName;
				}
			}
			return StringUtils.EMPTY;
		case Zuweiser:
			contact = coverageService.getRequiredContact(coverage, "Zuweiser");
			if (contact != null) {
				return contact.getPostalAddress();
			}
			return StringUtils.EMPTY;
		case ZuweiserKuerzel:
			contact = coverageService.getRequiredContact(coverage, "Zuweiser");
			if (contact != null) {
				String label = contact.getLabel();
				if (label.lastIndexOf(",") > 0) {
					String fullName = label.substring(0, label.indexOf(","));
					return fullName;
				}
			}
			return StringUtils.EMPTY;
		default:
			return null;
		}
	}

	private enum FallAttribute implements ILocalizedEnum {
		Versicherungsnummer("Versicherungsnummer des Fall"), Kostentraeger("Kostenträger des Fall"),
		KostentraegerKuerzel("Kürzel des Kostenträger des Fall"), KostentraegerOrt("Ort des Kostenträger des Fall"),
		Arbeitgeber("Arbeitgeber des Fall"), ArbeitgeberKuerzel("Kürzel des Arbeitgeber des Fall"),
		Zuweiser("Zuweiser des Fall"), ZuweiserKuerzel("Kürzel des Zuweiser des Fall");

		final String description;

		private FallAttribute(String description) {
			this.description = description;
		}

		@Override
		public String getLocaleText() {
			return description;
		}
	}

}
