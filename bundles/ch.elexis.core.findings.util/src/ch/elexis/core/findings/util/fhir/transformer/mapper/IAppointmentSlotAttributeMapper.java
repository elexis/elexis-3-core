package ch.elexis.core.findings.util.fhir.transformer.mapper;

import java.util.Set;

import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Schedule;
import org.hl7.fhir.r4.model.Slot;
import org.hl7.fhir.r4.model.Slot.SlotStatus;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.util.fhir.IFhirTransformerException;
import ch.elexis.core.findings.util.fhir.transformer.helper.IAppointmentHelper;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.agenda.Area;
import ch.elexis.core.services.IAppointmentService;

public class IAppointmentSlotAttributeMapper implements IdentifiableDomainResourceAttributeMapper<IAppointment, Slot> {

	private IAppointmentHelper appointmentHelper;

	private IAppointmentService appointmentService;

	public IAppointmentSlotAttributeMapper(IAppointmentService appointmentService) {
		this.appointmentService = appointmentService;
		appointmentHelper = new IAppointmentHelper();
	}

	@Override
	public void elexisToFhir(IAppointment elexis, Slot fhir, SummaryEnum summaryEnum, Set<Include> includes) {

		appointmentHelper.setVersionedIdPartLastUpdatedMeta(Appointment.class, fhir, elexis);

		fhir.getMeta().setLastUpdated(appointmentHelper.getLastUpdateAsDate(elexis.getLastupdate()).orElse(null));

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
	public void fhirToElexis(Slot fhir, IAppointment elexis) {

		String idPart = fhir.getSchedule().getReferenceElement().getIdPart();
		Area areaByNameOrId = appointmentService.getAreaByNameOrId(idPart);
		if (areaByNameOrId != null) {
			elexis.setSchedule(areaByNameOrId.getName());
		} else {
			throw new IFhirTransformerException("WARNING", "Referenced schedule not found", 412);
		}

		appointmentHelper.mapApplyStartEndMinutes(elexis, fhir);
	}

}
