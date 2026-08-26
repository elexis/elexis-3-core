package ch.elexis.core.fhir.mapper.r4;

import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Schedule;
import org.hl7.fhir.r4.model.Slot;
import org.hl7.fhir.r4.model.Slot.SlotStatus;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.fhir.mapper.r4.helper.IAppointmentHelper;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.agenda.Area;
import ch.elexis.core.services.IAppointmentService;

public class IAppointmentSlotAttributeMapper extends IdentifiableDomainResourceAttributeMapper<IAppointment, Slot> {

	private IAppointmentHelper appointmentHelper;

	private IAppointmentService appointmentService;

	public IAppointmentSlotAttributeMapper(IAppointmentService appointmentService) {
		super(Slot.class);

		this.appointmentService = appointmentService;
		appointmentHelper = new IAppointmentHelper();
	}

	@Override
	public void fullElexisToFhir(IAppointment elexis, Slot fhir, SummaryEnum summaryEnum) {

		Area area = appointmentService.getAreaByNameOrId(elexis.getSchedule());
		if (area != null) {
			fhir.setSchedule(new Reference(new IdType(Schedule.class.getSimpleName(), area.getId())));
		} else {
			// TODO the appointment has an area set, which is not configured!
			LoggerFactory.getLogger(getClass()).warn(
					"Appointment [{}] claims schedule id [{}] which is not configured. Not setting value.",
					elexis.getId(), elexis.getSchedule());
		}

		// TODO
		fhir.setStatus(SlotStatus.BUSY);

		appointmentHelper.mapApplyStartEndMinutes(fhir, elexis);
	}

	@Override
	public void fullFhirToElexis(Slot fhir, IAppointment elexis) {

		String idPart = fhir.getSchedule().getReferenceElement().getIdPart();
		Area areaByNameOrId = appointmentService.getAreaByNameOrId(idPart);
		if (areaByNameOrId != null) {
			elexis.setSchedule(areaByNameOrId.getName());
		} else {
			throw new AttributeMapperException("WARNING", "Referenced schedule not found", 412);
		}

		appointmentHelper.mapApplyStartEndMinutes(elexis, fhir);
	}

}
