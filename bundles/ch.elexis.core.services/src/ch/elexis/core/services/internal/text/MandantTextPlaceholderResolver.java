package ch.elexis.core.services.internal.text;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.interfaces.ILocalizedEnum;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.format.AddressFormatUtil;
import ch.elexis.core.model.format.PersonFormatUtil;
import ch.elexis.core.services.IContext;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.text.ITextPlaceholderResolver;
import ch.elexis.core.text.PlaceholderAttribute;

@Component
public class MandantTextPlaceholderResolver implements ITextPlaceholderResolver {
	
	@Override
	public String getSupportedType(){
		return "Mandant";
	}
	
	@Override
	public List<PlaceholderAttribute> getSupportedAttributes(){
		return Arrays.asList(MandantAttribute.values()).stream()
			.map(m -> new PlaceholderAttribute(getSupportedType(), m.name(), m.getLocaleText()))
			.collect(Collectors.toList());
	}
	
	@Override
	public Optional<String> replaceByTypeAndAttribute(IContext context, String attribute){
		IMandator mandantor = context.getTyped(IMandator.class).orElse(null);
		if (mandantor != null) {
			return Optional.ofNullable(replace(mandantor, attribute.toLowerCase()));
		}
		return Optional.empty();
	}
	
	private String replace(IMandator mandator, String lcAttribute){
		
		MandantAttribute mandantAttribut = searchEnum(MandantAttribute.class, lcAttribute);
		switch (mandantAttribut) {
		case Anrede:
			if (mandator.isPerson()) {
				return PersonFormatUtil.getSalutation(
					CoreModelServiceHolder.get().load(mandator.getId(), IPerson.class).get());
			} else {
				return "";
			}
		case Name:
			return mandator.getDescription1();
		case Vorname:
			return mandator.getDescription2();
		case Titel:
			if (mandator.isPerson()) {
				return CoreModelServiceHolder.get().load(mandator.getId(), IPerson.class).get()
					.getTitel();
			}
		case Anschrift:
			return AddressFormatUtil.getPostalAddress(mandator, true);
		case Anschriftzeile:
			return AddressFormatUtil.getPostalAddress(mandator, false);
		default:
			return null;
		}
	}
	
	private enum MandantAttribute implements ILocalizedEnum {
			Name("Nachname des Mandanten"), Vorname("Vorname des Mandanten"),
			Titel("Titel des Mandanten"), Anrede("Anrede des Mandanten"),
			Anschrift("Mehrzeilige Anschrift"), Anschriftzeile("Einzeilige Anschrift");
		
		final String description;
		
		private MandantAttribute(String description){
			this.description = description;
		}
		
		@Override
		public String getLocaleText(){
			return description;
		}
	}
	
}
