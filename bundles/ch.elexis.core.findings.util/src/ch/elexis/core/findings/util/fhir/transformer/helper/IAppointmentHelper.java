package ch.elexis.core.findings.util.fhir.transformer.helper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Slot.SlotStatus;
import org.hl7.fhir.r4.model.StringType;

import ch.elexis.core.model.IAppointment;
import ch.elexis.core.services.IConfigService;

public class IAppointmentHelper extends AbstractHelper {
	
	public SlotStatus getSlotStatus(IAppointment localObject){
		String type = localObject.getType();
		
		// TODO we need a dynamic mapping in the core, like it
		// is already present for RH, for example:
		switch (type) {
		case "frei":
			return SlotStatus.FREE;
		case "gesperrt":
			return SlotStatus.BUSYUNAVAILABLE;
		default:
			return SlotStatus.BUSY;
		}
	}
	
	public String getDescription(IAppointment localObject){
		String grund = localObject.getReason();
		if (grund == null || grund.length() < 1) {
			return localObject.getType();
		}
		return grund;
	}
	
	/**
	 * Map and apply the source status to the target status
	 * 
	 * @param target
	 * @param source
	 */
	public void mapApplyAppointmentStatus(Appointment target, IAppointment source,
		IConfigService configService){
		
		String state = source.getState();
		
		Extension statusExtension = new Extension();
		statusExtension.setUrl("http://elexis.info/codeelement/config/appointment-status");
		statusExtension.setValue(new StringType(state));
		
		// see ch.elexis.agenda.preferences.PreferenceConstants
		String color = configService.getActiveUserContact("agenda/farben/status/" + state, null);
		if (color != null) {
			Extension ucc = new Extension("user-configured-color", new StringType("#" + color));
			statusExtension.addExtension(ucc);
		}
	
		target.getExtension().add(statusExtension);
	}
	
	// TODO Extension contains both a value and nested extensions
	// maybe change?
	
	public void mapApplyAppointmentType(Appointment target, IAppointment source,
		IConfigService configService){
		
		String type = source.getType();
		
		Extension typeExtension = new Extension();
		typeExtension.setUrl("http://elexis.info/codeelement/config/appointment-type");
		typeExtension.setValue(new StringType(type));
		
		// see ch.elexis.agenda.preferences.PreferenceConstants
		String color = configService.getActiveUserContact("agenda/farben/typ/" + type, null);
		if (color != null) {
			Extension ucc = new Extension("user-configured-color", new StringType("#" + color));
			typeExtension.addExtension(ucc);
		}
		
		target.getExtension().add(typeExtension);
	}
	
	/**
	 * Map and apply start, end and duration
	 * 
	 * @param appointment
	 * @param localObject
	 */
	public void mapApplyStartEndMinutes(Appointment appointment, IAppointment localObject){
		LocalDateTime start = localObject.getStartTime();
		if (start != null) {
			Date start_ = Date.from(ZonedDateTime.of(start, ZoneId.systemDefault()).toInstant());
			appointment.setStart(start_);
		}
		
		LocalDateTime end = localObject.getEndTime();
		if (end != null) {
			Date end_ = Date.from(ZonedDateTime.of(end, ZoneId.systemDefault()).toInstant());
			appointment.setEnd(end_);
		}
		
		Integer durationMinutes = localObject.getDurationMinutes();
		if (durationMinutes != null) {
			appointment.setMinutesDuration(durationMinutes);
		}
	}
	
	//	/**
	//	 * Determine the {@link IAppointment#getSchedule()}. Currently only maps to a practitioners
	//	 * schedule
	//	 * 
	//	 * @param coreModelService
	//	 * @param appointmentService
	//	 * 
	//	 * @param fhirObject
	//	 * @return the schedule name if it could be determined, else <code>null</code>
	//	 * @deprecated
	//	 */
	//	public String mapSchedule(IModelService coreModelService,
	//		IAppointmentService appointmentService, Appointment fhirObject){
	//		
	//		// resolve by participant.actor.practitioner
	//		Optional<AppointmentParticipantComponent> actorPractitioner = fhirObject.getParticipant()
	//			.stream().filter(e -> e.getActor() != null).filter(e -> StringUtils
	//				.startsWith(e.getActor().getReference(), Practitioner.class.getSimpleName()))
	//			.findFirst();
	//		if (actorPractitioner.isPresent()) {
	//			String practitionerId =
	//				actorPractitioner.get().getActor().getReferenceElement().getIdPart();
	//			Optional<IContact> practitioner = coreModelService.load(practitionerId, IContact.class);
	//			if (practitioner.isEmpty()) {
	//				throw new IFhirTransformerException("WARNING", "Unresolvable practitioner", 0);
	//			}
	//			String contactArea =
	//				appointmentService.resolveAreaByAssignedContact(practitioner.get());
	//			if (contactArea != null) {
	//				return contactArea;
	//			} else {
	//				throw new IFhirTransformerException("WARNING",
	//					"No schedule assigned to practitioner", 0);
	//			}
	//		}
	//		
	//		// resolve via ?? SLOT ??
	//		
	//		return null;
	//	}
	
}
