package ch.elexis.core.findings.util.fhir.transformer;


import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.r4.model.Appointment;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.transformer.mapper.IAppointmentAppointmentAttributeMapper;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.services.IAppointmentService;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.holder.AppointmentHistoryServiceHolder;

@Component
public class AppointmentTerminTransformer implements IFhirTransformer<Appointment, IAppointment> {

	@org.osgi.service.component.annotations.Reference(target = "(" + IModelService.SERVICEMODELNAME
			+ "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@org.osgi.service.component.annotations.Reference
	private IAppointmentService appointmentService;

	@org.osgi.service.component.annotations.Reference
	private IConfigService configService;

	private IAppointmentAppointmentAttributeMapper attributeMapper;

	@Activate
	private void activate() {
		attributeMapper = new IAppointmentAppointmentAttributeMapper(appointmentService, coreModelService,
				configService);
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Appointment.class.equals(fhirClazz) && IAppointment.class.equals(localClazz);
	}

	@Override
	public Optional<IAppointment> getLocalObject(Appointment fhirObject) {
		String id = fhirObject.getIdElement().getIdPart();
		if (id != null && !id.isEmpty()) {
			return coreModelService.load(id, IAppointment.class);
		}
		return Optional.empty();
	}

	@Override
	public Optional<IAppointment> createLocalObject(Appointment fhirObject) {
		// requires an assigned area/schedule, which is accessible via Slot only
		throw new UnsupportedOperationException("Create Slot first, then perform update operation using Slot id.");
	}

	@Override
	public Optional<Appointment> getFhirObject(IAppointment localObject, SummaryEnum summaryEnum,
			Set<Include> includes) {

		Appointment appointment = new Appointment();
		attributeMapper.elexisToFhir(localObject, appointment, summaryEnum, includes);
		return Optional.of(appointment);
	}

	@Override
	public Optional<IAppointment> updateLocalObject(Appointment fhirObject, IAppointment localObject) {
		String originalReason = localObject.getReason();
		LocalDateTime originalEndTime = localObject.getEndTime();
		attributeMapper.fhirToElexis(fhirObject, localObject);
		if (!Objects.equals(originalEndTime, localObject.getEndTime())) {
			if (originalEndTime != null && localObject.getEndTime() != null) {
				AppointmentHistoryServiceHolder.get().logAppointmentDurationChange(localObject, originalEndTime,
						localObject.getEndTime());
			}
		}
		if (!Objects.equals(originalReason, fhirObject.getDescription())) {
			AppointmentHistoryServiceHolder.get().logAppointmentEdit(localObject);
		}
		coreModelService.save(localObject);
		return Optional.of(localObject);
	}
}
