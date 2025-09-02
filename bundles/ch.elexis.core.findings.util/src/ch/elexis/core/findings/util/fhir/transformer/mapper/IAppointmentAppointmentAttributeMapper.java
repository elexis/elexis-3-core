package ch.elexis.core.findings.util.fhir.transformer.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.Appointment.AppointmentParticipantComponent;
import org.hl7.fhir.r4.model.Appointment.AppointmentStatus;
import org.hl7.fhir.r4.model.Appointment.ParticipantRequired;
import org.hl7.fhir.r4.model.Appointment.ParticipationStatus;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Slot;
import org.hl7.fhir.r4.model.StringType;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.transformer.helper.FhirUtil;
import ch.elexis.core.findings.util.fhir.transformer.helper.IAppointmentHelper;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMandator;
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
			IModelService coreModelService, IConfigService configService) {
		this.appointmentService = appointmentService;
		this.coreModelService = coreModelService;
		this.configService = configService;
		appointmentHelper = new IAppointmentHelper();
	}

	@Override
	public void elexisToFhir(IAppointment localObject, Appointment appointment, SummaryEnum summaryEnum,
			Set<Include> includes) {

		FhirUtil.setVersionedIdPartLastUpdatedMeta(Appointment.class, appointment, localObject);

		// narrative
		appointmentHelper.setNarrative(appointment, localObject.getSubjectOrPatient());

		// Currently formal status is always booked, "real" elexis status and type
		// are transported via extension
		appointment.setStatus(AppointmentStatus.BOOKED);
		appointmentHelper.mapApplyAppointmentStateAndType(appointment, localObject, configService);

		appointment.setDescription(appointmentHelper.getDescription(localObject));

		appointmentHelper.mapApplyStartEndMinutes(appointment, localObject);

		Reference slotReference = new Reference(new IdType(Slot.class.getSimpleName(), localObject.getId()));
		if (includes.contains(new Include("Appointment:slot"))) {
			@SuppressWarnings("rawtypes")
			Optional<IFhirTransformer> _slotTransformer = OsgiServiceUtil.getService(IFhirTransformer.class,
					"(" + IFhirTransformer.TRANSFORMERID + "=Slot.IAppointment)");
			if (_slotTransformer.isPresent()) {
				@SuppressWarnings("unchecked")
				Slot _slot = (Slot) _slotTransformer.get().getFhirObject(localObject).orElse(null);
				slotReference.setResource(_slot);
				OsgiServiceUtil.ungetService(_slotTransformer.get());
			} else {
				LoggerFactory.getLogger(getClass()).error("Could not get slotTransformer service");
			}

		}
		appointment.setSlot(Collections.singletonList(slotReference));

		// actor.practitioner
		Optional<IContact> assignedContact = appointmentService.resolveAreaAssignedContact(localObject.getSchedule());
		if (assignedContact.isPresent() && assignedContact.get().isMandator()) {
			Reference practitionerReference = new Reference(
					new IdDt(Practitioner.class.getSimpleName(), assignedContact.get().getId()));

			if (includes.contains(new Include("Appointment:actor"))) {
				@SuppressWarnings("rawtypes")
				Optional<IFhirTransformer> _practitionerTransformer = OsgiServiceUtil.getService(IFhirTransformer.class,
						"(" + IFhirTransformer.TRANSFORMERID + "=Practitioner.IPerson)");
				if (_practitionerTransformer.isPresent()) {
					IMandator localMandator = coreModelService.load(assignedContact.get().getId(), IMandator.class)
							.get();
					@SuppressWarnings("unchecked")
					Practitioner _practitioner = (Practitioner) _practitionerTransformer.get()
							.getFhirObject(localMandator.asIPerson()).get();
					practitionerReference.setResource(_practitioner);
					OsgiServiceUtil.ungetService(_practitionerTransformer.get());
				} else {
					LoggerFactory.getLogger(getClass()).error("Could not get patientTransformer service");
				}
			}

			AppointmentParticipantComponent hcp = appointment.addParticipant();
			hcp.setActor(practitionerReference);
			hcp.setRequired(ParticipantRequired.REQUIRED);
			hcp.setStatus(ParticipationStatus.ACCEPTED);
		}

		// actor.patient
		IContact contact = localObject.getContact();
		if (contact != null && contact.isPatient()) {
			// formal patient appointment
			Reference patientReference = new Reference(new IdType(Patient.class.getSimpleName(), contact.getId()));

			if (includes.contains(new Include("Appointment:actor"))
					|| includes.contains(new Include("Appointment:patient"))) {
				@SuppressWarnings("rawtypes")
				Optional<IFhirTransformer> _patientTransformer = OsgiServiceUtil.getService(IFhirTransformer.class,
						"(" + IFhirTransformer.TRANSFORMERID + "=Patient.IPatient)");
				if (_patientTransformer.isPresent()) {
					IPatient localPatient = coreModelService.load(contact.getId(), IPatient.class).get();
					@SuppressWarnings("unchecked")
					Patient _patient = (Patient) _patientTransformer.get().getFhirObject(localPatient).get();
					patientReference.setResource(_patient);
					OsgiServiceUtil.ungetService(_patientTransformer.get());
				} else {
					LoggerFactory.getLogger(getClass()).error("Could not get patientTransformer service");
				}
			}

			AppointmentParticipantComponent patient = appointment.addParticipant();
			patient.setActor(patientReference);
			patient.setRequired(ParticipantRequired.REQUIRED);
			patient.setStatus(ParticipationStatus.ACCEPTED);

		} else {
			// free-text-appointment
			String subject = localObject.getSubjectOrPatient();
			appointment.setComment(subject);
		}

		if (appointment.getParticipant().isEmpty()) {
			// participant is mandatory
			AppointmentParticipantComponent participant = appointment.addParticipant();
			participant.setStatus(ParticipationStatus.ACCEPTED);
		}

		String stateHistoryFormatted = localObject.getStateHistoryFormatted("dd.MM.yyyy HH:mm:ss");
		if (StringUtils.isNotEmpty(stateHistoryFormatted)) {
			// TODO move status history to Appointment#setNote(Annotation) in next version
			Extension historyExtension = new Extension();
			historyExtension.setUrl("http://elexis.info/appointment/");

			Extension _historyExtension = new Extension("history", new StringType(stateHistoryFormatted));
			historyExtension.addExtension(_historyExtension);

			appointment.getExtension().add(historyExtension);
		}

	}

	@Override
	public void fhirToElexis(Appointment source, IAppointment target) {

		appointmentHelper.mapApplyAppointmentStateAndType(target, source);
		appointmentHelper.mapApplyStartEndMinutes(target, source);

		target.setSubjectOrPatient(null);
		List<AppointmentParticipantComponent> participant = source.getParticipant();
		for (AppointmentParticipantComponent appointmentParticipantComponent : participant) {
			Reference actorTarget = appointmentParticipantComponent.getActor();
			IdType idType = new IdType(actorTarget.getReference());
			if (Patient.class.getSimpleName().equals(idType.getResourceType())) {
				target.setSubjectOrPatient(idType.getIdPart());
			}
		}
		if (StringUtils.isEmpty(target.getSubjectOrPatient())) {
			target.setSubjectOrPatient(source.getComment());
		}

		target.setReason(source.getDescription());

		// TODO what else in subject or patient if no patient set?
	}

}
