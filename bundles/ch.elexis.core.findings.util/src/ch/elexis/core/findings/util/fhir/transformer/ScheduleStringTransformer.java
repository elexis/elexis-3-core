package ch.elexis.core.findings.util.fhir.transformer;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Schedule;
import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.agenda.Area;
import ch.elexis.core.model.agenda.AreaType;
import ch.elexis.core.services.IAppointmentService;
import ch.elexis.core.services.IModelService;

@Component
public class ScheduleStringTransformer implements IFhirTransformer<Schedule, String> {

	@org.osgi.service.component.annotations.Reference(target = "(" + IModelService.SERVICEMODELNAME
			+ "=ch.elexis.core.model)")
	private IModelService modelService;

	@org.osgi.service.component.annotations.Reference
	private IAppointmentService appointmentService;

	@Override
	public Optional<Schedule> getFhirObject(String localObject, SummaryEnum summaryEnum, Set<Include> includes) {

		Area areaByNameOrId = appointmentService.getAreaByNameOrId(localObject);
		Schedule schedule = (areaByNameOrId != null) ? transformToFhir(areaByNameOrId) : null;

		return Optional.ofNullable(schedule);
	}

	@Override
	public Optional<String> getLocalObject(Schedule fhirObject) {
		Area areaByNameOrId = appointmentService.getAreaByNameOrId(fhirObject.getId());
		if (areaByNameOrId != null) {
			return Optional.of(areaByNameOrId.getName());
		}
		return Optional.empty();
	}

	@Override
	public Optional<String> updateLocalObject(Schedule fhirObject, String localObject) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<String> createLocalObject(Schedule fhirObject) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Schedule.class.equals(fhirClazz) && String.class.equals(localClazz);
	}

	private Schedule transformToFhir(Area area) {
		Schedule schedule = new Schedule();

		// id might not be rest compatible, if we use the plain area name
		schedule.setId(new IdDt(Schedule.class.getSimpleName(), area.getId()));

		schedule.setActive(true);

		AreaType type = area.getType();
		if (Objects.equals(AreaType.CONTACT, type)) {
			Optional<IContact> assignedContact = appointmentService.resolveAreaAssignedContact(area.getName());
			if (assignedContact.isPresent()) {
				Reference actor = new Reference(
						new IdDt(Practitioner.class.getSimpleName(), assignedContact.get().getId()));
				schedule.getActor().add(actor);
			} else {
				LoggerFactory.getLogger(getClass()).warn("Could not resolve contact [{}]", area.getContactId());
			}
		}

		schedule.getText().setStatus(org.hl7.fhir.r4.model.Narrative.NarrativeStatus.GENERATED);
		schedule.getText().setDivAsString(area.getName());

		return schedule;
	}

}
