package ch.elexis.core.findings.util.fhir.accessor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Encounter.DiagnosisComponent;
import org.hl7.fhir.r4.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.r4.model.Encounter.EncounterStatus;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;

import org.apache.commons.lang3.StringUtils;

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.IdentifierSystem;
import ch.elexis.core.findings.util.ModelUtil;
import ch.elexis.core.findings.util.fhir.transformer.helper.FhirUtil;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMandator;

public class EncounterAccessor extends AbstractFindingsAccessor {

	public Optional<LocalDateTime> getStartTime(DomainResource resource) {
		org.hl7.fhir.r4.model.Encounter fhirEncounter = (org.hl7.fhir.r4.model.Encounter) resource;
		Period period = fhirEncounter.getPeriod();
		if (period != null && period.getStart() != null) {
			return Optional.of(getLocalDateTime(period.getStart()));
		}
		return Optional.empty();
	}

	public void setStartTime(DomainResource resource, LocalDateTime time) {
		org.hl7.fhir.r4.model.Encounter fhirEncounter = (org.hl7.fhir.r4.model.Encounter) resource;
		Period period = fhirEncounter.getPeriod();
		if (period == null) {
			period = new Period();
		}
		period.setStart(getDate(time));
		fhirEncounter.setPeriod(period);
	}

	public Optional<LocalDateTime> getEndTime(DomainResource resource) {
		org.hl7.fhir.r4.model.Encounter fhirEncounter = (org.hl7.fhir.r4.model.Encounter) resource;
		Period period = fhirEncounter.getPeriod();
		if (period != null && period.getEnd() != null) {
			return Optional.of(getLocalDateTime(period.getEnd()));
		}
		return Optional.empty();
	}

	public void setEndTime(DomainResource resource, LocalDateTime time) {
		org.hl7.fhir.r4.model.Encounter fhirEncounter = (org.hl7.fhir.r4.model.Encounter) resource;
		Period period = fhirEncounter.getPeriod();
		if (period == null) {
			period = new Period();
		}
		period.setEnd(getDate(time));
		fhirEncounter.setPeriod(period);
	}

	public List<ICondition> getIndication(DomainResource resource) {
		List<ICondition> indication = new ArrayList<>();
		org.hl7.fhir.r4.model.Encounter fhirEncounter = (org.hl7.fhir.r4.model.Encounter) resource;
		List<DiagnosisComponent> theIndication = fhirEncounter.getDiagnosis();
		for (DiagnosisComponent component : theIndication) {
			Reference reference = component.getCondition();
			if (reference.getReference() != null) {
				String idString = reference.getReferenceElement().getIdPart();
				ModelUtil.loadFinding(idString, ICondition.class).ifPresent(condition -> indication.add(condition));
			}
		}
		return indication;
	}

	public void setIndication(DomainResource resource, List<ICondition> indication) {
		org.hl7.fhir.r4.model.Encounter fhirEncounter = (org.hl7.fhir.r4.model.Encounter) resource;
		List<DiagnosisComponent> theIndication = new ArrayList<>();
		for (ICondition iCondition : indication) {
			theIndication.add(new DiagnosisComponent(new Reference(new IdDt("Condition", iCondition.getId()))));
		}
		fhirEncounter.setDiagnosis(theIndication);
	}

	public List<ICoding> getType(DomainResource resource) {
		org.hl7.fhir.r4.model.Encounter fhirEncounter = (org.hl7.fhir.r4.model.Encounter) resource;
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

	public void setType(DomainResource resource, List<ICoding> coding) {
		org.hl7.fhir.r4.model.Encounter fhirEncounter = (org.hl7.fhir.r4.model.Encounter) resource;
		List<CodeableConcept> codeableConcepts = fhirEncounter.getType();
		if (!codeableConcepts.isEmpty()) {
			codeableConcepts.clear();
		}
		CodeableConcept codeableConcept = new CodeableConcept();
		ModelUtil.setCodingsToConcept(codeableConcept, coding);
		fhirEncounter.setType(Collections.singletonList(codeableConcept));
	}

	public void setPatientId(DomainResource resource, String patientId) {
		org.hl7.fhir.r4.model.Encounter fhirEncounter = (org.hl7.fhir.r4.model.Encounter) resource;
		fhirEncounter.setSubject(new Reference(new IdDt("Patient", patientId)));
	}

	/**
	 * Read Coverage (Elexis Fall) reference from Encounter.account[*].
	 * Allows FHIR clients to explicitly assign a consultation to a specific
	 * Fall instead of being forced into the auto-created "online" default fall.
	 */
	public Optional<String> getCoverageId(DomainResource resource) {
		org.hl7.fhir.r4.model.Encounter fhirEncounter = (org.hl7.fhir.r4.model.Encounter) resource;
		if (fhirEncounter.hasAccount()) {
			for (Reference account : fhirEncounter.getAccount()) {
				if (account != null && FhirUtil.isReferenceType(account, "Coverage")) {
					return FhirUtil.getId(account);
				}
			}
		}
		return Optional.empty();
	}

	/**
	 * Expose the Elexis Fall id as Encounter.account[0] so that FHIR
	 * clients can see which Fall a consultation belongs to.
	 */
	public void setCoverageId(DomainResource resource, String coverageId) {
		org.hl7.fhir.r4.model.Encounter fhirEncounter = (org.hl7.fhir.r4.model.Encounter) resource;
		fhirEncounter.getAccount().clear();
		if (StringUtils.isNotBlank(coverageId)) {
			fhirEncounter.addAccount(new Reference(new IdDt("Coverage", coverageId)));
		}
	}

	/**
	 * Map Elexis IEncounter.billable to FHIR Encounter.status. A billable
	 * (still editable) consultation maps to in-progress, a non-billable
	 * (closed/billed) one to finished.
	 */
	public void setStatus(DomainResource resource, boolean billable) {
		org.hl7.fhir.r4.model.Encounter fhirEncounter = (org.hl7.fhir.r4.model.Encounter) resource;
		fhirEncounter.setStatus(billable ? EncounterStatus.INPROGRESS : EncounterStatus.FINISHED);
	}

	/**
	 * Derive the billable flag from FHIR Encounter.status, so that clients
	 * can reopen / close a consultation via PUT.
	 */
	public Optional<Boolean> getBillable(DomainResource resource) {
		org.hl7.fhir.r4.model.Encounter fhirEncounter = (org.hl7.fhir.r4.model.Encounter) resource;
		if (!fhirEncounter.hasStatus()) {
			return Optional.empty();
		}
		switch (fhirEncounter.getStatus()) {
		case PLANNED:
		case ARRIVED:
		case TRIAGED:
		case INPROGRESS:
		case ONLEAVE:
			return Optional.of(Boolean.TRUE);
		case FINISHED:
		case CANCELLED:
		case ENTEREDINERROR:
		case UNKNOWN:
		case NULL:
		default:
			return Optional.of(Boolean.FALSE);
		}
	}

	public void setConsultationId(DomainResource resource, String consultationId) {
		org.hl7.fhir.r4.model.Encounter fhirEncounter = (org.hl7.fhir.r4.model.Encounter) resource;
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
		org.hl7.fhir.r4.model.Encounter fhirEncounter = (org.hl7.fhir.r4.model.Encounter) resource;
		EncounterParticipantComponent participant = new EncounterParticipantComponent();
		participant.setIndividual(new Reference("Practitioner/" + mandatorId));
		fhirEncounter.addParticipant(participant);
	}

	public void setPrimaryPerformer(Encounter target, IMandator mandator) {
		EncounterParticipantComponent participant = new EncounterParticipantComponent();
		participant.getTypeFirstRep().addCoding(
				new Coding("http://hl7.org/fhir/ValueSet/encounter-participant-type", "PPRF", "primary performer"));
		participant.setIndividual(new Reference("Practitioner/" + mandator.getId()));
		target.addParticipant(participant);
	}

	public void setAttender(Encounter target, IContact mandator) {
		EncounterParticipantComponent participant = new EncounterParticipantComponent();
		participant.getTypeFirstRep().addCoding(
				new Coding("http://hl7.org/fhir/ValueSet/encounter-participant-type", "ATND", "attender"));
		participant.setIndividual(new Reference("Practitioner/" + mandator.getId()));
		target.addParticipant(participant);

	}
}
