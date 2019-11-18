package ch.elexis.core.services.internal.text;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.interfaces.ILocalizedEnum;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IContext;
import ch.elexis.core.text.ITextPlaceholderResolver;
import ch.elexis.core.text.PlaceholderAttribute;

@Component
public class PatientTextPlaceholderResolver implements ITextPlaceholderResolver {
	
	@Override
	public String getSupportedType(){
		return "Patient";
	}
	
	@Override
	public List<PlaceholderAttribute> getSupportedAttributes(){
		return Arrays.asList(PatientAttribute.values()).stream()
			.map(m -> new PlaceholderAttribute(getSupportedType(), m.name(), m.getLocaleText()))
			.collect(Collectors.toList());
	}
	
	@Override
	public Optional<String> replaceByTypeAndAttribute(IContext context, String attribute){
		IPatient patient = context.getTyped(IPatient.class).orElse(null);
		if (patient != null) {
			return Optional.ofNullable(replace(patient, attribute.toLowerCase()));
		}
		return Optional.empty();
	}
	
	private String replace(IPatient patient, String lcAttribute){
		
		PatientAttribute patientAttribut = searchEnum(PatientAttribute.class, lcAttribute);
		switch (patientAttribut) {
		case Name:
			return patient.getLastName();
		case Vorname:
			return patient.getFirstName();
		default:
			break;
		}
		return null;
	}
	
	private enum PatientAttribute implements ILocalizedEnum {
			Name("Tag des Termins im Format dd.MM.yyyy"), Vorname("Zugehöriger Bereich");
		
		final String description;
		
		private PatientAttribute(String description){
			this.description = description;
		}
		
		@Override
		public String getLocaleText(){
			return description;
		}
	}
	
}
