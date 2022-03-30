package ch.elexis.core.findings.util.fhir.transformer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Schedule;
import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.codes.CodingSystem;
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
		Schedule schedule = getSchedules().get(localObject);
		return Optional.ofNullable(schedule);
	}

	@Override
	public Optional<String> getLocalObject(Schedule fhirObject) {
		if (getSchedules().containsKey(fhirObject.getId())) {
			return Optional.of(getSchedules().get(fhirObject.getId()).getId());
		}
		return Optional.empty();
	}

	private Map<String, Schedule> getSchedules() {
		Map<String, Schedule> schedules = new HashMap<>();

		List<Area> areas = appointmentService.getAreas();
		for (Area area : areas) {
			Schedule schedule = new Schedule();

			String areaId = DigestUtils.md5Hex(area.getName());
			// id might not be rest compatible, if we use the plain area name
			schedule.setId(new IdDt(Schedule.class.getSimpleName(), areaId));

			Identifier identifier = new Identifier();
			identifier.setSystem(CodingSystem.ELEXIS_AGENDA_AREA_ID.getSystem());
			identifier.setValue(area.getName());
			schedule.addIdentifier(identifier);

			schedule.setActive(true);

			AreaType type = area.getType();
			Reference actor;
			if (Objects.equals(AreaType.CONTACT, type)) {
				Optional<IContact> assignedContact = appointmentService.resolveAreaAssignedContact(area.getContactId());
				if (assignedContact.isPresent()) {
					actor = new Reference(new IdDt(Practitioner.class.getSimpleName(), assignedContact.get().getId()));
				} else {
					LoggerFactory.getLogger(getClass()).warn("Could not resolve contact [{}]", area.getContactId());
					actor = null;
				}
			} else {
				actor = new Reference(
						new IdDt(Location.class.getSimpleName(), LocationStringTransformer.MAIN_LOCATION));
			}

			schedule.getActor().add(actor);

			CodeableConcept areaType = new CodeableConcept();
			areaType.addCoding(new Coding(CodingSystem.ELEXIS_AGENDA_AREA_TYPE.getSystem(), type.toString(), ""));
			schedule.setServiceCategory(Collections.singletonList(areaType));
			schedule.getServiceCategory();

			schedule.setComment(area.getName());

			schedules.put(areaId, schedule);
		}

		return schedules;
	}

	@Override
	public Optional<String> updateLocalObject(Schedule fhirObject, String localObject) {
		return Optional.empty();
	}

	@Override
	public Optional<String> createLocalObject(Schedule fhirObject) {
		return Optional.empty();
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Schedule.class.equals(fhirClazz) && String.class.equals(localClazz);
	}

}
