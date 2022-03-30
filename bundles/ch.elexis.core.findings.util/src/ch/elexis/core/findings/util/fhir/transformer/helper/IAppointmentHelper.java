package ch.elexis.core.findings.util.fhir.transformer.helper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.Appointment.AppointmentParticipantComponent;
import org.hl7.fhir.r4.model.Appointment.AppointmentStatus;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Slot.SlotStatus;

import ch.elexis.core.findings.util.fhir.IFhirTransformerException;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.IContact;
import ch.elexis.core.services.IAppointmentService;
import ch.elexis.core.services.IModelService;

public class IAppointmentHelper extends AbstractHelper {

	public SlotStatus getSlotStatus(IAppointment localObject) {
		String type = localObject.getType();

		// TODO we need a dynamic mapping in the core, like it
		// is already present for RH, for example:
		switch (type) {
			case "frei" :
				return SlotStatus.FREE;
			case "gesperrt" :
				return SlotStatus.BUSYUNAVAILABLE;
			default :
				return SlotStatus.BUSY;
		}
	}

	public String getDescription(IAppointment localObject) {
		String grund = localObject.getReason();
		if (grund == null || grund.length() < 1) {
			return localObject.getType();
		}
		return grund;
	}

	public void mapApplyAppointmentStatus(Appointment target, IAppointment source) {
		String appointmentState = source.getState();

		// TODO we need a dynamic mapping in the core, like it
		// is already present for RH, for example:
		switch (appointmentState) {
			case "eingetroffen" :
				target.setStatus(AppointmentStatus.ARRIVED);
				return;
			case "erledigt" :
				target.setStatus(AppointmentStatus.FULFILLED);
				return;
			case "abgesagt" :
				target.setStatus(AppointmentStatus.CANCELLED);
				return;
			case "nicht erschienen" :
				target.setStatus(AppointmentStatus.NOSHOW);
				return;
			default :
				target.setStatus(AppointmentStatus.BOOKED);
		}
	}

	/**
	 * Map and apply the source status to the target status
	 * 
	 * @param target
	 * @param source
	 */
	public void mapApplyAppointmentStatus(IAppointment target, Appointment source) {
		AppointmentStatus status = source.getStatus();
		switch (status) {
			case FULFILLED :
				target.setState("erledigt");
				return;
			case ARRIVED :
				target.setState("eingetroffen");
				return;
			case CANCELLED :
				target.setState("abgesagt");
				return;
			case NOSHOW :
				target.setState("nicht erschienen");
				return;
			default :
				target.setState("-");
		}

	}

	/**
	 * Map and apply start, end and duration
	 * 
	 * @param appointment
	 * @param localObject
	 */
	public void mapApplyStartEndMinutes(Appointment appointment, IAppointment localObject) {
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

	/**
	 * Determine the {@link IAppointment#getSchedule()}. Currently only maps to a
	 * practitioners schedule
	 * 
	 * @param coreModelService
	 * @param appointmentService
	 * 
	 * @param fhirObject
	 * @return the schedule name if it could be determined, else <code>null</code>
	 */
	public String mapSchedule(IModelService coreModelService, IAppointmentService appointmentService,
			Appointment fhirObject) {

		// resolve by participant.actor.practitioner
		Optional<AppointmentParticipantComponent> actorPractitioner = fhirObject.getParticipant().stream()
				.filter(e -> e.getActor() != null)
				.filter(e -> StringUtils.startsWith(e.getActor().getReference(), Practitioner.class.getSimpleName()))
				.findFirst();
		if (actorPractitioner.isPresent()) {
			String practitionerId = actorPractitioner.get().getActor().getReferenceElement().getIdPart();
			Optional<IContact> practitioner = coreModelService.load(practitionerId, IContact.class);
			if (practitioner.isEmpty()) {
				throw new IFhirTransformerException("WARNING", "Unresolvable practitioner", 0);
			}
			String contactArea = appointmentService.resolveAreaByAssignedContact(practitioner.get());
			if (contactArea != null) {
				return contactArea;
			} else {
				throw new IFhirTransformerException("WARNING", "No schedule assigned to practitioner", 0);
			}
		}

		// resolve via ?? SLOT ??

		return null;
	}

}
