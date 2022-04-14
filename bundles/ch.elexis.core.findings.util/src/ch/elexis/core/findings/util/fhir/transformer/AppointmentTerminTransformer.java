package ch.elexis.core.findings.util.fhir.transformer;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.Appointment.AppointmentParticipantComponent;
import org.hl7.fhir.r4.model.Appointment.AppointmentStatus;
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
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;

@Component
public class AppointmentTerminTransformer implements IFhirTransformer<Appointment, IAppointment> {
	
	@org.osgi.service.component.annotations.Reference(target = "(" + IFhirTransformer.TRANSFORMERID
		+ "=Patient.IPatient)")
	private IFhirTransformer<Patient, IPatient> patientTransformer;
	
	@org.osgi.service.component.annotations.Reference(target = "(" + IFhirTransformer.TRANSFORMERID
		+ "=Slot.IAppointment)")
	private IFhirTransformer<Slot, IAppointment> slotTransformer;
	
	@org.osgi.service.component.annotations.Reference(target = "(" + IModelService.SERVICEMODELNAME
		+ "=ch.elexis.core.model)")
	private IModelService coreModelService;
	
	@org.osgi.service.component.annotations.Reference
	private IAppointmentService appointmentService;
	
	@org.osgi.service.component.annotations.Reference
	private IContextService contextService;
	
	@org.osgi.service.component.annotations.Reference
	private IConfigService configService;
	
	private IAppointmentHelper appointmentHelper;
	
	@Activate
	private void activate(){
		appointmentHelper = new IAppointmentHelper();
	}
	
	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz){
		return Appointment.class.equals(fhirClazz) && IAppointment.class.equals(localClazz);
	}
	
	@Override
	public Optional<IAppointment> getLocalObject(Appointment fhirObject){
		String id = fhirObject.getIdElement().getIdPart();
		if (id != null && !id.isEmpty()) {
			return coreModelService.load(id, IAppointment.class);
		}
		return Optional.empty();
	}
	
	@Override
	public Optional<IAppointment> createLocalObject(Appointment fhirObject){
		
		//		String area = appointmentHelper.mapSchedule(coreModelService, appointmentService, fhirObject);
		//		System.out.println(area);
		// TODO to determine bereich may require Slot first?
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Optional<Appointment> getFhirObject(IAppointment localObject, SummaryEnum summaryEnum,
		Set<Include> includes){
		Appointment appointment = new Appointment();
		
		appointment.setId(new IdDt(Appointment.class.getSimpleName(), localObject.getId()));
		appointment.getMeta().setVersionId(localObject.getLastupdate().toString());
		appointment.getMeta().setLastUpdated(
			appointmentHelper.getLastUpdateAsDate(localObject.getLastupdate()).orElse(null));
		
		appointmentHelper.setText(appointment, localObject.getLabel());
		
		// Currently formal status is always booked, "real" elexis status and type
		// are transported via extension
		appointment.setStatus(AppointmentStatus.BOOKED);
		appointmentHelper.mapApplyAppointmentStatus(appointment, localObject, configService);
		appointmentHelper.mapApplyAppointmentType(appointment, localObject, configService);
		
		appointment.setDescription(appointmentHelper.getDescription(localObject));
		
		appointmentHelper.mapApplyStartEndMinutes(appointment, localObject);
		
		Reference slotReference =
			new Reference(new IdType(Slot.class.getSimpleName(), localObject.getId()));
		if (includes.contains(new Include("Appointment.slot"))) {
			Slot _slot = slotTransformer.getFhirObject(localObject).orElse(null);
			slotReference.setResource(_slot);
		}
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
		
		IContact contact = localObject.getContact();
		if (contact != null && contact.isPatient()) {
			IPatient localPatient = coreModelService.load(contact.getId(), IPatient.class).get();
			AppointmentParticipantComponent patient = new AppointmentParticipantComponent();
			patient.setActor(
				new Reference(new IdDt(Patient.class.getSimpleName(), localPatient.getId())));
			patient.setRequired(ParticipantRequired.REQUIRED);
			patient.setStatus(ParticipationStatus.ACCEPTED);
			
			if (includes.contains(new Include("Appointment.patient"))) {
				Patient _patient = patientTransformer.getFhirObject(localPatient).get();
				patient.getActor().setResource(_patient);
			}
			participant.add(patient);
		} else {
			// TODO there is another string inside - where to put it? is it relevant?
			String subjectOrPatient = localObject.getSubjectOrPatient();
		}
		
		return Optional.of(appointment);
	}
	
	@Override
	public Optional<IAppointment> updateLocalObject(Appointment fhirObject,
		IAppointment localObject){
		
		// determine bereich either via participant or ?
		
		//		appointmentHelper.mapApplyAppointmentStatus(localObject, fhirObject);
		// TODO more
		
		coreModelService.save(localObject);
		return Optional.empty();
	}
	
}
