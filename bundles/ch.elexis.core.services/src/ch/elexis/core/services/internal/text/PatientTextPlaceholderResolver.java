package ch.elexis.core.services.internal.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.interfaces.ILocalizedEnum;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.format.PatientFormatUtil;
import ch.elexis.core.model.format.PersonFormatUtil;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.IContext;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.text.ITextPlaceholderResolver;
import ch.elexis.core.text.PlaceholderAttribute;

@Component
public class PatientTextPlaceholderResolver implements ITextPlaceholderResolver {

	@Reference(target = "(type=Kontakt)")
	private ITextPlaceholderResolver contactTextPlaceholderResolver;

	@Override
	public String getSupportedType() {
		return "Patient";
	}

	@Override
	public List<PlaceholderAttribute> getSupportedAttributes() {
		List<PlaceholderAttribute> ret = new ArrayList<>();
		ret.addAll(Arrays.asList(PatientAttribute.values()).stream()
				.map(m -> new PlaceholderAttribute(getSupportedType(), m.name(), m.getLocaleText()))
				.collect(Collectors.toList()));
		if (contactTextPlaceholderResolver != null) {
			ret.addAll(contactTextPlaceholderResolver.getSupportedAttributes());
		}
		return ret;
	}

	@Override
	public Optional<String> replaceByTypeAndAttribute(IContext context, String attribute) {
		IPatient patient = (IPatient) getIdentifiable(context).orElse(null);
		if (patient != null) {
			return Optional.ofNullable(replace(patient, attribute.toLowerCase()));
		}
		return Optional.empty();
	}

	@Override
	public Optional<? extends Identifiable> getIdentifiable(IContext context) {
		return context.getTyped(IPatient.class);
	}

	private String replace(IPatient patient, String lcAttribute) {

		PatientAttribute patientAttribut = searchEnum(PatientAttribute.class, lcAttribute);
		if (patientAttribut != null) {
			switch (patientAttribut) {
			case Anrede:
				return PersonFormatUtil.getSalutation(patient);
			case Name:
				return patient.getLastName();
			case Vorname:
				return patient.getFirstName();
			case Geburtsdatum:
				return PersonFormatUtil.getDateOfBirth(patient);
			case Diagnosen:
				return patient.getDiagnosen();
			case Allergien:
				return patient.getAllergies();
			case Medikation:
				return PatientFormatUtil.getMedicationText(patient, EntryType.FIXED_MEDICATION);
			case SymptomatischeMedikation:
				return PatientFormatUtil.getMedicationText(patient, EntryType.SYMPTOMATIC_MEDICATION);
			case ReserveMedikation:
				return PatientFormatUtil.getMedicationText(patient, EntryType.RESERVE_MEDICATION);
			default:
				break;
			}
		}
		// fallback to contact properties
		if (contactTextPlaceholderResolver != null) {
			IContact contact = CoreModelServiceHolder.get().load(patient.getId(), IContact.class).get();
			TextPlaceholderContext context = new TextPlaceholderContext(contact);
			Optional<String> contactReplacement = contactTextPlaceholderResolver.replaceByTypeAndAttribute(context,
					lcAttribute);
			if(contactReplacement.isPresent()) {
				return contactReplacement.get();
			}
		}
		return null;
	}

	private enum PatientAttribute implements ILocalizedEnum {
		Name("Nachname des Patienten"), Vorname("Vorname des Patienten"), Anrede("Anrede des Patienten"),
		Geburtsdatum("Geburtsdatum des Patienten"), Diagnosen("Diagnosen des Patienten"),
		Allergien("Allergien des Patienten"), Medikation("Medikation des Patienten"),
		SymptomatischeMedikation("Symptomatische Medikation des Patienten"),
		ReserveMedikation("Reserve Medikation des Patienten");

		final String description;

		private PatientAttribute(String description) {
			this.description = description;
		}

		@Override
		public String getLocaleText() {
			return description;
		}
	}

}
