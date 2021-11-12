package ch.elexis.core.findings.util.fhir.transformer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.Appointment.AppointmentParticipantComponent;
import org.hl7.fhir.r4.model.Appointment.ParticipantRequired;
import org.hl7.fhir.r4.model.Appointment.ParticipationStatus;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Slot;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.transformer.helper.IAppointmentHelper;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IAppointmentService;
import ch.elexis.core.services.IModelService;

@Component
public class AppointmentTerminTransformer implements IFhirTransformer<Appointment, IAppointment> {
	
	@org.osgi.service.component.annotations.Reference(target = "(" + IFhirTransformer.TRANSFORMERID
		+ "=Patient.IPatient)")
	private IFhirTransformer<Patient, IPatient> patientTransformer;
	
	@org.osgi.service.component.annotations.Reference(target = "(" + IModelService.SERVICEMODELNAME
		+ "=ch.elexis.core.model)")
	private IModelService modelService;
	
	@org.osgi.service.component.annotations.Reference
	private IAppointmentService appointmentService;
	
	private IAppointmentHelper appointmentHelper;
	
	@Activate
	private void activate(){
		appointmentHelper = new IAppointmentHelper();
	}
	
	@Override
	public Optional<Appointment> getFhirObject(IAppointment localObject, SummaryEnum summaryEnum,
		Set<Include> includes){
		Appointment appointment = new Appointment();
		
		appointment.setId(new IdDt(Appointment.class.getSimpleName(), localObject.getId()));
		appointment.getMeta().setVersionId(localObject.getLastupdate().toString());
		appointment.getMeta().setLastUpdated(
			appointmentHelper.getLastUpdateAsDate(localObject.getLastupdate()).orElse(null));
		
		appointmentHelper.mapApplyAppointmentStatus(appointment, localObject);
		
		appointment.setDescription(appointmentHelper.getDescription(localObject));
		
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
		
		Reference slotReference =
			new Reference(new IdType(Slot.class.getSimpleName(), localObject.getId()));
		appointment.setSlot(Collections.singletonList(slotReference));
		
		List<AppointmentParticipantComponent> participant = appointment.getParticipant();
		
		Optional<IContact> assignedContact =
			appointmentService.resolveAreaAssignedContact(localObject.getSchedule());
		if (assignedContact.isPresent() && assignedContact.get().isMandator()) {
			AppointmentParticipantComponent hcp = new AppointmentParticipantComponent();
			hcp.setActor(new Reference(
				new IdDt(Practitioner.class.getSimpleName(), assignedContact.get().getId())));
			hcp.setRequired(ParticipantRequired.REQUIRED);
			hcp.setStatus(ParticipationStatus.ACCEPTED);
			participant.add(hcp);
		}
		
		String patientIdOrSomeString = localObject.getSubjectOrPatient();
		if (StringUtils.isNotEmpty(patientIdOrSomeString)) {
			Optional<IPatient> patientContact =
				modelService.load(patientIdOrSomeString, IPatient.class);
			if (patientContact.isPresent()) {
				AppointmentParticipantComponent patient = new AppointmentParticipantComponent();
				patient.setActor(
					new Reference(new IdDt(Patient.class.getSimpleName(), patientIdOrSomeString)));
				patient.setRequired(ParticipantRequired.REQUIRED);
				patient.setStatus(ParticipationStatus.ACCEPTED);
				participant.add(patient);
				
				if (includes.contains(new Include("Appointment:patient"))) {
					patient.getActor()
						.setResource(patientTransformer.getFhirObject(patientContact.get()).get());
				}
			} else {
				// TODO there is another string inside - where to put it? is it relevant?
			}
		}
		
		return Optional.of(appointment);
	}
	
	@Override
	public Optional<IAppointment> getLocalObject(Appointment fhirObject){
		String id = fhirObject.getIdElement().getIdPart();
		if (id != null && !id.isEmpty()) {
			return modelService.load(id, IAppointment.class);
		}
		return Optional.empty();
	}
	
	@Override
	public Optional<IAppointment> updateLocalObject(Appointment fhirObject,
		IAppointment localObject){
		
		appointmentHelper.mapApplyAppointmentStatus(localObject, fhirObject);
		// TODO more
		
		modelService.save(localObject);
		return Optional.empty();
	}
	
	@Override
	public Optional<IAppointment> createLocalObject(Appointment fhirObject){
		return Optional.empty();
	}
	
	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz){
		return Appointment.class.equals(fhirClazz) && IAppointment.class.equals(localClazz);
	}
	
}
