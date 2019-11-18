package ch.elexis.core.services.internal.text;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.interfaces.ILocalizedEnum;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.services.IContext;
import ch.elexis.core.text.ITextPlaceholderResolver;
import ch.elexis.core.text.PlaceholderAttribute;
import ch.rgw.tools.TimeTool;

@Component
public class TerminTextPlaceholderResolver implements ITextPlaceholderResolver {
	
	@Override
	public String getSupportedType(){
		return "Termin";
	}
	
	@Override
	public List<PlaceholderAttribute> getSupportedAttributes(){
		return Arrays.asList(TerminAttribute.values()).stream()
			.map(m -> new PlaceholderAttribute(getSupportedType(), m.name(), m.getLocaleText()))
			.collect(Collectors.toList());
	}
	
	@Override
	public Optional<String> replaceByTypeAndAttribute(IContext context, String attribute){
		IAppointment appointment = context.getTyped(IAppointment.class).orElse(null);
		if (appointment != null) {
			return Optional.ofNullable(replace(appointment, attribute.toLowerCase()));
		}
		return Optional.empty();
	}
	
	private String replace(IAppointment appointment, String lcAttribute){
		LocalDateTime value;
		
		TerminAttribute attribute = searchEnum(TerminAttribute.class, lcAttribute);
		switch (attribute) {
		case Tag:
			value = appointment.getStartTime();
			return new TimeTool(value).toString(TimeTool.DATE_GER);
		case Bereich:
			return appointment.getSchedule();
		case Zeit:
			value = appointment.getStartTime();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
			return formatter.format(value);
		default:
			return null;
		}
	}
	
	private enum TerminAttribute implements ILocalizedEnum {
			Tag("Tag des Termins im Format dd.MM.yyyy"), Bereich("Zugehöriger Bereich"),
			Zeit("Startzeitpunkt im Format hh:mm");
		
		final String description;
		
		private TerminAttribute(String description){
			this.description = description;
		}
		
		@Override
		public String getLocaleText(){
			return description;
		}
	}
	
}
