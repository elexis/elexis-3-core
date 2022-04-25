package ch.elexis.core.findings.util.fhir.transformer.mapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Set;

import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Schedule;
import org.hl7.fhir.r4.model.Slot;
import org.hl7.fhir.r4.model.Slot.SlotStatus;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.agenda.Area;
import ch.elexis.core.services.IAppointmentService;
import ch.elexis.core.time.TimeUtil;

public class IAppointmentSlotAttributeMapper implements IdentifiableDomainResourceAttributeMapper<IAppointment, Slot> {

	private IAppointmentService appointmentService;

	public IAppointmentSlotAttributeMapper(IAppointmentService appointmentService) {
		this.appointmentService = appointmentService;
	}

	@Override
	public void elexisToFhir(IAppointment elexis, Slot fhir, SummaryEnum summaryEnum, Set<Include> includes) {

		fhir.setId(new IdDt(Slot.class.getSimpleName(), elexis.getId()));

		fhir.setSchedule(new Reference(new IdType(Schedule.class.getSimpleName(),
				appointmentService.getAreaByNameOrId(elexis.getSchedule()).getId())));

		// TODO
		fhir.setStatus(SlotStatus.BUSY);

		LocalDateTime start = elexis.getStartTime();
		if (start != null) {
			Date start_ = Date.from(ZonedDateTime.of(start, ZoneId.systemDefault()).toInstant());
			fhir.setStart(start_);
		} else {
			// TODO is required - what now?
		}

		LocalDateTime end = elexis.getEndTime();
		if (end != null) {
			Date end_ = Date.from(ZonedDateTime.of(end, ZoneId.systemDefault()).toInstant());
			fhir.setEnd(end_);
		} else {
			// TODO is required - what now?
		}

	}

	@Override
	public void fhirToElexis(Slot fhir, IAppointment elexis) {

		String idPart = fhir.getSchedule().getReferenceElement().getIdPart();
		Area areaByNameOrId = appointmentService.getAreaByNameOrId(idPart);
		elexis.setSchedule(areaByNameOrId.getName());

		Date start = fhir.getStart();
		elexis.setStartTime(TimeUtil.toLocalDateTime(start));

		Date end = fhir.getEnd();
		elexis.setEndTime(TimeUtil.toLocalDateTime(end));
	}

}
