package ch.elexis.core.findings.util.fhir.accessor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.IdentifierSystem;
import ch.elexis.core.findings.util.ModelUtil;

public class EncounterAccessor extends AbstractFindingsAccessor {
	
	public Optional<LocalDateTime> getStartTime(DomainResource resource){
		org.hl7.fhir.dstu3.model.Encounter fhirEncounter =
			(org.hl7.fhir.dstu3.model.Encounter) resource;
		Period period = fhirEncounter.getPeriod();
		if (period != null && period.getStart() != null) {
			return Optional.of(getLocalDateTime(period.getStart()));
		}
		return Optional.empty();
	}
	
	public void setStartTime(DomainResource resource, LocalDateTime time){
		org.hl7.fhir.dstu3.model.Encounter fhirEncounter =
			(org.hl7.fhir.dstu3.model.Encounter) resource;
		Period period = fhirEncounter.getPeriod();
		if (period == null) {
			period = new Period();
		}
		period.setStart(getDate(time));
		fhirEncounter.setPeriod(period);
	}
	
	public Optional<LocalDateTime> getEndTime(DomainResource resource){
		org.hl7.fhir.dstu3.model.Encounter fhirEncounter =
			(org.hl7.fhir.dstu3.model.Encounter) resource;
		Period period = fhirEncounter.getPeriod();
		if (period != null && period.getEnd() != null) {
			return Optional.of(getLocalDateTime(period.getEnd()));
		}
		return Optional.empty();
	}
	
	public void setEndTime(DomainResource resource, LocalDateTime time){
		org.hl7.fhir.dstu3.model.Encounter fhirEncounter =
			(org.hl7.fhir.dstu3.model.Encounter) resource;
		Period period = fhirEncounter.getPeriod();
		if (period == null) {
			period = new Period();
		}
		period.setEnd(getDate(time));
		fhirEncounter.setPeriod(period);
	}
	
	public List<ICondition> getIndication(DomainResource resource, IFindingsService service){
		List<ICondition> indication = new ArrayList<>();
		org.hl7.fhir.dstu3.model.Encounter fhirEncounter =
			(org.hl7.fhir.dstu3.model.Encounter) resource;
		List<Reference> theIndication = fhirEncounter.getIndication();
		for (Reference reference : theIndication) {
			if (reference.getReference() != null
				&& reference.getReference().contains("Condition")) {
				String idString = reference.getReferenceElement().getIdPart();
				service.findById(idString, ICondition.class)
					.ifPresent(condition -> indication.add((ICondition) condition));
			}
		}
		return indication;
	}
	
	public void setIndication(DomainResource resource, List<ICondition> indication){
		org.hl7.fhir.dstu3.model.Encounter fhirEncounter =
			(org.hl7.fhir.dstu3.model.Encounter) resource;
		List<Reference> theIndication = new ArrayList<>();
		for (ICondition iCondition : indication) {
			theIndication.add(new Reference(new IdDt("Condition", iCondition.getId())));
		}
		fhirEncounter.setIndication(theIndication);
	}
	
	public List<ICoding> getType(DomainResource resource){
		org.hl7.fhir.dstu3.model.Encounter fhirEncounter =
			(org.hl7.fhir.dstu3.model.Encounter) resource;
		List<CodeableConcept> codeableConcepts = fhirEncounter.getType();
		if (codeableConcepts != null) {
			ArrayList<ICoding> ret = new ArrayList<>();
			for (CodeableConcept codeableConcept : codeableConcepts) {
				ret.addAll(ModelUtil.getCodingsFromConcept(codeableConcept));
			}
			return ret;
		}
		return Collections.emptyList();
	}
	
	public void setType(DomainResource resource, List<ICoding> coding){
		org.hl7.fhir.dstu3.model.Encounter fhirEncounter =
			(org.hl7.fhir.dstu3.model.Encounter) resource;
		List<CodeableConcept> codeableConcepts = fhirEncounter.getType();
		if (!codeableConcepts.isEmpty()) {
			codeableConcepts.clear();
		}
		CodeableConcept codeableConcept = new CodeableConcept();
		ModelUtil.setCodingsToConcept(codeableConcept, coding);
		fhirEncounter.setType(Collections.singletonList(codeableConcept));
	}

	public void setPatientId(DomainResource resource, String patientId) {
		org.hl7.fhir.dstu3.model.Encounter fhirEncounter = (org.hl7.fhir.dstu3.model.Encounter) resource;
		fhirEncounter.setPatient(new Reference(new IdDt("Patient", patientId)));
	}

	public void setConsultationId(DomainResource resource, String consultationId) {
		org.hl7.fhir.dstu3.model.Encounter fhirEncounter = (org.hl7.fhir.dstu3.model.Encounter) resource;
		boolean identifierFound = false;
		List<Identifier> existing = fhirEncounter.getIdentifier();
		for (Identifier existingIdentifier : existing) {
			if (IdentifierSystem.ELEXIS_CONSID.getSystem().equals(existingIdentifier.getSystem())) {
				existingIdentifier.setValue(consultationId);
				identifierFound = true;
				break;
			}
		}
		if (!identifierFound) {
			Identifier identifier = fhirEncounter.addIdentifier();
			identifier.setSystem(IdentifierSystem.ELEXIS_CONSID.getSystem());
			identifier.setValue(consultationId);
		}
	}

	public void setMandatorId(DomainResource resource, String mandatorId) {
		org.hl7.fhir.dstu3.model.Encounter fhirEncounter = (org.hl7.fhir.dstu3.model.Encounter) resource;
		EncounterParticipantComponent participant = new EncounterParticipantComponent();
		participant.setIndividual(new Reference("Practitioner/" + mandatorId));
		fhirEncounter.addParticipant(participant);
	}
}
