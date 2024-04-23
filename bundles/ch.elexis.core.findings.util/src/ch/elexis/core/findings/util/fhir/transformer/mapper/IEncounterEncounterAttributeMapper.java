package ch.elexis.core.findings.util.fhir.transformer.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.r4.model.Encounter;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.util.ModelUtil;
import ch.elexis.core.findings.util.fhir.accessor.EncounterAccessor;
import ch.elexis.core.findings.util.model.TransientCoding;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.holder.EncounterServiceHolder;
import ch.elexis.core.text.model.Samdas;
import ch.elexis.core.text.model.Samdas.Record;
import ch.rgw.tools.VersionedResource;

public class IEncounterEncounterAttributeMapper
		implements IdentifiableDomainResourceAttributeMapper<IEncounter, Encounter> {

	private EncounterAccessor accessor = new EncounterAccessor();

	@Override
	public void elexisToFhir(IEncounter source, Encounter target, SummaryEnum summaryEnum, Set<Include> includes) {

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

		accessor.setPrimaryPerformer(target, source.getMandator());
		if (source.getMandator().getBiller().isPerson() && source.getMandator().getBiller().isMandator()
				&& !source.getMandator().equals(source.getMandator().getBiller())) {
			accessor.setAttender(target, source.getMandator().getBiller());			
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
	public void fhirToElexis(Encounter source, IEncounter target) {
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
				EncounterServiceHolder.get().updateVersionedEntry(cons, samdas);
			}
		}
	}
}
