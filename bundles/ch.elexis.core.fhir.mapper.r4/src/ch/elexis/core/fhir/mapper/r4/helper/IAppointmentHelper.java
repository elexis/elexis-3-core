package ch.elexis.core.fhir.mapper.r4.helper;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Slot;
import org.hl7.fhir.r4.model.StringType;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IAppointment;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.time.TimeUtil;

public class IAppointmentHelper extends AbstractHelper {

	public String getDescription(IAppointment localObject) {
		return localObject.getReason();
	}

	/**
	 * ELEXIS -> FHIR: Map and apply the source state and type to the target status
	 *
	 * @param target
	 * @param source
	 */
	public void mapApplyAppointmentStateAndType(Appointment target, IAppointment source, IConfigService configService) {

		Extension extension = new Extension();
		extension.setUrl("http://elexis.info/codeelement/config/appointment/");

		String state = source.getState();
		Extension statusExtension = new Extension("state", new StringType(state));
		extension.addExtension(statusExtension);

		String type = source.getType();
		Extension typeExtension = new Extension("type", new StringType(type));
		extension.addExtension(typeExtension);

		target.getExtension().add(extension);
	}

	/**
	 * FHIR -> ELEXIS: Map and apply the source status and type to the target status
	 *
	 * @param target
	 * @param source
	 */
	public void mapApplyAppointmentStateAndType(IAppointment target, Appointment source) {
		// FIXME all that is not set, is to be removed, otherwise how to depict
		// deletes??
		Extension extensionByUrl = source.getExtensionByUrl("http://elexis.info/codeelement/config/appointment/");
		if (extensionByUrl != null) {
			Extension statusExtension = extensionByUrl.getExtensionByUrl("state");
			if (statusExtension != null) {
				String status = statusExtension.getValue().toString();
				// TODO check if valid?
				target.setState(status);
			}

			Extension typeExtension = extensionByUrl.getExtensionByUrl("type");
			if (typeExtension != null) {
				String type = typeExtension.getValue().toString();
				// TODO check if valid
				target.setType(type);
			}
		}
		// FIXME what if none set?
	}

	/**
	 * ELEXIS -> FHIR: Map and apply start, end and duration
	 *
	 * @param target
	 * @param source
	 */
	public void mapApplyStartEndMinutes(Slot target, IAppointment source) {
		LocalDateTime start = source.getStartTime();
		if (start != null) {
			Date start_ = TimeUtil.toDate(start);
			target.setStart(start_);

			if (source.isAllDay()) {
				LocalDateTime endOfDay = start.toLocalDate().atTime(LocalTime.MAX);
				Date _endOfDay = TimeUtil.toDate(endOfDay);
				target.setEnd(_endOfDay);
				return;
			}
		}

		LocalDateTime end = source.getEndTime();
		if (end != null) {
			Date end_ = TimeUtil.toDate(end);
			target.setEnd(end_);
		}
	}

	/**
	 * ELEXIS -> FHIR: Map and apply start, end and duration
	 *
	 * @param target
	 * @param source
	 */
	public void mapApplyStartEndMinutes(Appointment target, IAppointment source) {
		LocalDateTime start = source.getStartTime();
		if (start != null) {
			Date start_ = TimeUtil.toDate(start);
			target.setStart(start_);

			if (source.isAllDay()) {
				LocalDateTime endOfDay = start.toLocalDate().atTime(LocalTime.MAX);
				Date _endOfDay = TimeUtil.toDate(endOfDay);
				target.setEnd(_endOfDay);
				return;
			}
		}

		LocalDateTime end = source.getEndTime();
		if (end != null) {
			Date end_ = TimeUtil.toDate(end);
			target.setEnd(end_);
		}

		Integer durationMinutes = source.getDurationMinutes();
		if (durationMinutes != null) {
			target.setMinutesDuration(durationMinutes);
		}
	}

	/**
	 * FHIR -> ELEXIS: Map and apply start, end and duration
	 *
	 * @param target
	 * @param source
	 */
	public void mapApplyStartEndMinutes(IAppointment target, Appointment source) {
		mapApplyStartEndMinutes(target, source.getStart(), source.getEnd());
	}

	/**
	 * FHIR -> ELEXIS: Map and apply start, end and duration
	 *
	 * @param target
	 * @param source
	 */
	public void mapApplyStartEndMinutes(IAppointment target, Slot source) {
		mapApplyStartEndMinutes(target, source.getStart(), source.getEnd());
	}

	public void mapApplyStartEndMinutes(IAppointment target, Date start, Date end) {
		if (start == null) {
			// Elexis does not allow empty start
			start = new Date();
			LoggerFactory.getLogger(getClass()).warn("Appointment F->E [{}] no start time, setting now");
		}
		LocalDateTime _start = TimeUtil.toLocalDateTime(start);
		target.setStartTime(_start);

		if (end == null) {
			end = new Date(start.getTime() + (60 * 5 * 1000));
			LoggerFactory.getLogger(getClass()).warn("Appointment F->E [{}] no end time, setting to start+5m");
		}
		LocalDateTime _end = TimeUtil.toLocalDateTime(end);
		target.setEndTime(_end);

		if (_start.toLocalDate().atStartOfDay().withNano(0).equals(_start.withNano(0))
				&& _end.toLocalDate().atTime(LocalTime.MAX).withNano(0).equals(_end.withNano(0))) {
			// all day appointments qualify via
			// startTime 00:00:00.000
			// endTime 23:59:59.999
			target.setEndTime(null);
		}
	}

	// /**
	// * Determine the {@link IAppointment#getSchedule()}. Currently only maps to a
	// practitioners
	// * schedule
	// *
	// * @param coreModelService
	// * @param appointmentService
	// *
	// * @param fhirObject
	// * @return the schedule name if it could be determined, else <code>null</code>
	// * @deprecated
	// */
	// public String mapSchedule(IModelService coreModelService,
	// IAppointmentService appointmentService, Appointment fhirObject){
	//
	// // resolve by participant.actor.practitioner
	// Optional<AppointmentParticipantComponent> actorPractitioner =
	// fhirObject.getParticipant()
	// .stream().filter(e -> e.getActor() != null).filter(e -> StringUtils
	// .startsWith(e.getActor().getReference(), Practitioner.class.getSimpleName()))
	// .findFirst();
	// if (actorPractitioner.isPresent()) {
	// String practitionerId =
	// actorPractitioner.get().getActor().getReferenceElement().getIdPart();
	// Optional<IContact> practitioner = coreModelService.load(practitionerId,
	// IContact.class);
	// if (practitioner.isEmpty()) {
	// throw new IFhirTransformerException("WARNING", "Unresolvable practitioner",
	// 0);
	// }
	// String contactArea =
	// appointmentService.resolveAreaByAssignedContact(practitioner.get());
	// if (contactArea != null) {
	// return contactArea;
	// } else {
	// throw new IFhirTransformerException("WARNING",
	// "No schedule assigned to practitioner", 0);
	// }
	// }
	//
	// // resolve via ?? SLOT ??
	//
	// return null;
	// }

}
