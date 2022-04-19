package ch.elexis.core.findings.util.fhir.transformer.mapper;

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
import org.slf4j.LoggerFactory;

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
import ch.elexis.core.services.IModelService;
import ch.elexis.core.utils.OsgiServiceUtil;

public class IAppointmentAppointmentAttributeMapper
		implements IdentifiableDomainResourceAttributeMapper<IAppointment, Appointment> {
	
	private IAppointmentHelper appointmentHelper;
	private IAppointmentService appointmentService;
	private IConfigService configService;
	private IModelService coreModelService;
	
	public IAppointmentAppointmentAttributeMapper(IAppointmentService appointmentService,
		IModelService coreModelService, IConfigService configService){
		this.appointmentService = appointmentService;
		this.coreModelService = coreModelService;
		this.configService = configService;
		appointmentHelper = new IAppointmentHelper();
	}
	
	@Override
	public void elexisToFhir(IAppointment localObject, Appointment appointment,
		SummaryEnum summaryEnum, Set<Include> includes){
		
		appointment.setId(new IdDt(Appointment.class.getSimpleName(), localObject.getId()));
		
		appointment.getMeta().setVersionId(localObject.getLastupdate().toString());
		appointment.getMeta().setLastUpdated(
			appointmentHelper.getLastUpdateAsDate(localObject.getLastupdate()).orElse(null));
		
		appointmentHelper.setText(appointment, localObject.getLabel());
		
		// Currently formal status is always booked, "real" elexis status and type
		// are transported via extension
		appointment.setStatus(AppointmentStatus.BOOKED);
		appointmentHelper.mapApplyAppointmentStatusType(appointment, localObject, configService);
		
		appointment.setDescription(appointmentHelper.getDescription(localObject));
		
		appointmentHelper.mapApplyStartEndMinutes(appointment, localObject);
		
		Reference slotReference =
			new Reference(new IdType(Slot.class.getSimpleName(), localObject.getId()));
		if (includes.contains(new Include("Appointment.slot"))) {
			@SuppressWarnings("rawtypes")
			Optional<IFhirTransformer> _slotTransformer = OsgiServiceUtil.getService(
				IFhirTransformer.class, IFhirTransformer.TRANSFORMERID + "=Slot.IAppointment)");
			if (_slotTransformer.isPresent()) {
				@SuppressWarnings("unchecked")
				Slot _slot = (Slot) _slotTransformer.get().getFhirObject(localObject).orElse(null);
				slotReference.setResource(_slot);
			} else {
				LoggerFactory.getLogger(getClass()).error("Could not get slotTransformer service");
			}
			
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
				@SuppressWarnings("rawtypes")
				Optional<IFhirTransformer> _patientTransformer = OsgiServiceUtil.getService(
					IFhirTransformer.class, IFhirTransformer.TRANSFORMERID + "=Patient.IPatient)");
				if (_patientTransformer.isPresent()) {
					@SuppressWarnings("unchecked")
					Patient _patient =
						(Patient) _patientTransformer.get().getFhirObject(localPatient).get();
					patient.getActor().setResource(_patient);
				} else {
					LoggerFactory.getLogger(getClass())
						.error("Could not get patientTransformer service");
				}
			}
			participant.add(patient);
		} else {
			// TODO there is another string inside - where to put it? is it relevant?
			String subjectOrPatient = localObject.getSubjectOrPatient();
		}
		
	}
	
	@Override
	public void fhirToElexis(Appointment source, IAppointment target){
		
		appointmentHelper.mapApplyAppointmentStatusType(target, source);
		
		
	}
	
}
