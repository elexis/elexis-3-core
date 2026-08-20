package ch.elexis.core.fhir.mapper.r4;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.r4.model.Encounter;

import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.fhir.mapper.r4.findings.EncounterAccessor;
import ch.elexis.core.fhir.mapper.r4.util.ModelUtil;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.model.TransientCoding;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IEncounterService;
import ch.elexis.core.text.model.Samdas;
import ch.elexis.core.text.model.Samdas.Record;
import ch.rgw.tools.VersionedResource;

public class IEncounterEncounterAttributeMapper
		extends IdentifiableDomainResourceAttributeMapper<IEncounter, Encounter> {

	private IEncounterService encounterService;
	private EncounterAccessor accessor;

	public IEncounterEncounterAttributeMapper(IEncounterService encounterService) {
		super(Encounter.class);

		this.encounterService = encounterService;
		accessor = new EncounterAccessor();
	}

	@Override
	public void fullElexisToFhir(IEncounter source, Encounter target, SummaryEnum summaryEnum) {

		LocalDateTime encounterDateTime = source.getTimeStamp();
		LocalDate encounterDate = source.getDate();
		if (encounterDate != null) {
			accessor.setStartTime(target, encounterDateTime != null ? encounterDateTime : encounterDate.atStartOfDay());
			accessor.setEndTime(target, encounterDate.atTime(23, 59, 59));
		}
		ICoverage coverage = source.getCoverage();
		if (coverage != null) {
			IPatient patient = coverage.getPatient();
			if (patient != null) {
				accessor.setPatientId(target, patient.getId());
			}
		}

		if (source.getMandator() != null) {
			accessor.setPrimaryPerformer(target, source.getMandator());
			if (source.getMandator().getBiller().isPerson() && source.getMandator().getBiller().isMandator()
					&& !source.getMandator().equals(source.getMandator().getBiller())) {
				accessor.setAttender(target, source.getMandator().getBiller());
			}
		}

		VersionedResource vr = source.getVersionedEntry();
		if (vr != null) {
			Samdas samdas = new Samdas(vr.getHead());
			accessor.setText(target, samdas.getRecordText());
		}

		List<ICoding> coding = accessor.getType(target);
		if (!ModelUtil.isSystemInList(CodingSystem.ELEXIS_ENCOUNTER_TYPE.getSystem(), coding)) {
			coding.add(new TransientCoding(CodingSystem.ELEXIS_ENCOUNTER_TYPE.getSystem(), "text",
					"Nicht strukturierte Konsultation"));
			accessor.setType(target, coding);
		}
	}

	@Override
	public void fullFhirToElexis(Encounter source, IEncounter target) {
		if (source.hasPeriod()) {
			if (source.getPeriod().hasStart()) {
				target.setTimeStamp(
						LocalDateTime.ofInstant(source.getPeriod().getStart().toInstant(), ZoneId.systemDefault()));
			}
		}

		if (source.hasText()) {
			updateConsText(source, target);
		}
	}

	private void updateConsText(Encounter fhirObject, ch.elexis.core.model.IEncounter cons) {
		Optional<String> consText = ModelUtil.getNarrativeAsString(fhirObject.getText());
		if (consText.isPresent()) {
			String existingText = cons.getHeadVersionInPlaintext();
			if (!consText.get().equals(existingText)) {
				Samdas samdas = new Samdas(cons.getVersionedEntry().getHead());
				Record rec = samdas.getRecord();
				rec.setText(consText.get());
				encounterService.updateVersionedEntry(cons, samdas);
			}
		}
	}
}
