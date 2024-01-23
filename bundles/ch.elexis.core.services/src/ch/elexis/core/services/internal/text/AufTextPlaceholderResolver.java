package ch.elexis.core.services.internal.text;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;

import ch.elexis.core.interfaces.ILocalizedEnum;
import ch.elexis.core.model.ISickCertificate;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IContext;
import ch.elexis.core.text.ITextPlaceholderResolver;
import ch.elexis.core.text.PlaceholderAttribute;
import ch.elexis.core.time.TimeUtil;

@Component
public class AufTextPlaceholderResolver implements ITextPlaceholderResolver {

	@Override
	public String getSupportedType() {
		return "AUF";
	}

	@Override
	public List<PlaceholderAttribute> getSupportedAttributes() {
		return Arrays.asList(AufAttribute.values()).stream()
				.map(m -> new PlaceholderAttribute(getSupportedType(), m.name(), m.getLocaleText()))
				.collect(Collectors.toList());
	}

	@Override
	public Optional<String> replaceByTypeAndAttribute(IContext context, String attribute) {
		ISickCertificate sickCertificate = (ISickCertificate) getIdentifiable(context).orElse(null);
		if (sickCertificate != null) {
			return Optional.ofNullable(replace(sickCertificate, attribute.toLowerCase()));
		}
		return Optional.empty();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Optional<? extends Identifiable> getIdentifiable(IContext context) {
		Optional<ISickCertificate> ret = context.getTyped(ISickCertificate.class);
		if (ret.isEmpty()) {
			ret = (Optional<ISickCertificate>) context.getNamed(getSupportedType());
		}
		return ret;
	}

	private String replace(ISickCertificate sickCertificate, String lcAttribute) {
		AufAttribute attribute = searchEnum(AufAttribute.class, lcAttribute);
		switch (attribute) {
		case Grund:
			return StringUtils.defaultString(sickCertificate.getReason());
		case Von:
			return sickCertificate.getStart() != null ? TimeUtil.DATE_GER.format(sickCertificate.getStart())
					: StringUtils.EMPTY;
		case Bis:
			return sickCertificate.getEnd() != null ? TimeUtil.DATE_GER.format(sickCertificate.getEnd())
					: StringUtils.EMPTY;
		case Prozent:
			return Integer.toString(sickCertificate.getPercent());
		case Zusatz:
			return StringUtils.defaultString(sickCertificate.getNote());
		default:
			return null;
		}
	}

	private enum AufAttribute implements ILocalizedEnum {
		Grund("Grund der Arbeitsunfähigkeit"), Bis("Datum des Ende der Arbeitsunfähigkeit"),
		Prozent("Prozent der Arbeitsunfähigkeit"), Zusatz("Zusatz der Arbeitsunfähigkeit"),
		Von("Datum des Start der Arbeitsunfähigkeit");

		final String description;

		private AufAttribute(String description) {
			this.description = description;
		}

		@Override
		public String getLocaleText() {
			return description;
		}
	}
}
