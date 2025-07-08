package ch.elexis.core.services.internal.text;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.interfaces.ILocalizedEnum;
import ch.elexis.core.services.IContext;
import ch.elexis.core.text.ITextPlaceholderResolver;
import ch.elexis.core.text.PlaceholderAttribute;

@Component
public class ImageTextPlaceholderResolver implements ITextPlaceholderResolver {

	@Override
	public String getSupportedType() {
		return "Image";
	}

	@Override
	public List<PlaceholderAttribute> getSupportedAttributes() {
		return Arrays.asList(ImageAttribute.values()).stream()
				.map(m -> new PlaceholderAttribute(getSupportedType(), m.name(), m.getLocaleText()))
				.collect(Collectors.toList());
	}

	@Override
	public Optional<String> replaceByTypeAndAttribute(IContext context, String attribute) {
		return Optional.ofNullable(replace(attribute.toLowerCase()));
	}

	private String replace(String lcAttribute) {
		ImageAttribute imageAttribut = searchEnum(ImageAttribute.class, lcAttribute);
		if (imageAttribut != null) {
			switch (imageAttribut) {
			case MailPraxisLogo:
				return "<img src=\"cid:elexismailpraxislogo\" />";
			case MailAppointmentQr:
				return "<img src=\"cid:elexismailappointmentqr\" />";
			default:
				return null;
			}
		}
		return null;
	}

	private enum ImageAttribute implements ILocalizedEnum {
		MailPraxisLogo("Ein Praxis Logo für e-mails"), MailAppointmentQr("QR code eines Termins");

		final String description;

		private ImageAttribute(String description) {
			this.description = description;
		}

		@Override
		public String getLocaleText() {
			return description;
		}
	}
}
