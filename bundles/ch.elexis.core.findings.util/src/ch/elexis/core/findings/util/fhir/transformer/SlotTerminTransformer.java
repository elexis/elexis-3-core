package ch.elexis.core.findings.util.fhir.transformer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.r4.model.Slot;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.fhir.mapper.r4.IAppointmentSlotAttributeMapper;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.services.IAppointmentService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.holder.AppointmentHistoryServiceHolder;

@Component(property = IFhirTransformer.TRANSFORMERID + "=Slot.IAppointment")
public class SlotTerminTransformer implements IFhirTransformer<Slot, IAppointment> {

	@org.osgi.service.component.annotations.Reference(target = "(" + IModelService.SERVICEMODELNAME
			+ "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@org.osgi.service.component.annotations.Reference
	private IAppointmentService appointmentService;

	private IAppointmentSlotAttributeMapper attributeMapper;

	@Activate
	private void activate() {
		attributeMapper = new IAppointmentSlotAttributeMapper(appointmentService);
	}

	@Override
	public Optional<Slot> getFhirObject(IAppointment localObject, SummaryEnum summaryEnum, Set<Include> includes) {
		Slot slot = new Slot();
		attributeMapper.elexisToFhir(localObject, slot, summaryEnum);
		return Optional.of(slot);
	}

	@Override
	public Optional<IAppointment> getLocalObject(Slot fhirObject) {
		String id = fhirObject.getIdElement().getIdPart();
		if (id != null && !id.isEmpty()) {
			return coreModelService.load(id, IAppointment.class);
		}
		return Optional.empty();
	}

	@Override
	public Optional<IAppointment> updateLocalObject(Slot fhirObject, IAppointment localObject) {
		String originalArea = localObject.getSchedule();
		LocalDateTime oldStartTime = localObject.getStartTime();
		LocalDateTime newStartTime = fhirObject.getStart().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
				.withSecond(0).withNano(0);
		attributeMapper.fhirToElexis(fhirObject, localObject);
		String newArea = localObject.getSchedule();
		if (!originalArea.equals(newArea)) {
			AppointmentHistoryServiceHolder.get().logAppointmentMove(localObject, oldStartTime, newStartTime,
					originalArea, newArea);
		}
		coreModelService.save(localObject);
		return Optional.of(localObject);
	}

	@Override
	public Optional<IAppointment> createLocalObject(Slot fhirObject) {
		IAppointment create = coreModelService.create(IAppointment.class);
		attributeMapper.fhirToElexis(fhirObject, create);
		coreModelService.save(create);
		return Optional.of(create);
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Slot.class.equals(fhirClazz) && IAppointment.class.equals(localClazz);
	}

}
