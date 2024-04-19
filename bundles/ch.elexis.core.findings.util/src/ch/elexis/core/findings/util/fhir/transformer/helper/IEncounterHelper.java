package ch.elexis.core.findings.util.fhir.transformer.helper;

import java.util.List;
import java.util.Optional;

import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;

import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.FallConstants;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.builder.ICoverageBuilder;
import ch.elexis.core.model.builder.IEncounterBuilder;
import ch.elexis.core.services.IModelService;

public class IEncounterHelper extends AbstractHelper {

	private IModelService coreModelService;
	private IModelService findingsModelService;

	public IEncounterHelper(IModelService coreModelService, IModelService findingsModelService) {
		this.coreModelService = coreModelService;
		this.findingsModelService = findingsModelService;
	}

	public Optional<ch.elexis.core.model.IEncounter> createIEncounter(IEncounter iEncounter) {
		Optional<ch.elexis.core.model.IEncounter> ret = getIEncounter(iEncounter);
		if (!ret.isPresent()) {
			Optional<IPatient> patient = getPatient(iEncounter);
			Optional<IMandator> serviceProvider = getPerformer(iEncounter);
			if (patient.isPresent() && serviceProvider.isPresent()) {
				ICoverage fall = getOrCreateDefaultFall(patient.get());
				ch.elexis.core.model.IEncounter encounter = new IEncounterBuilder(coreModelService, fall,
						serviceProvider.get()).buildAndSave();
				findingsModelService.save(encounter);
				ret = Optional.of(encounter);
			}
		}
		return ret;
	}

	public ICoverage getOrCreateDefaultFall(IPatient patient) {
		List<ICoverage> coverages = patient.getCoverages();
		ICoverage defaultCoverage = lookUpDefaultFall(coverages);
		if (defaultCoverage == null) {
			defaultCoverage = createDefaultFall(patient);
			acquireAndReleaseLock(defaultCoverage);
		}
		return defaultCoverage;
	}

	private ICoverage createDefaultFall(IPatient patient) {
		ICoverage ret = new ICoverageBuilder(coreModelService, patient, "online", FallConstants.TYPE_DISEASE, "KVG")
				.buildAndSave();
		closeFall(ret);
		return ret;
	}

	private void closeFall(ICoverage fall) {
		LockResponse lockResponse = AbstractHelper.acquireLock(fall);
		if (lockResponse.isOk()) {
			fall.setDateTo(fall.getDateFrom());
			coreModelService.save(fall);
			AbstractHelper.releaseLock(lockResponse.getLockInfo());
		}
	}

	private ICoverage lookUpDefaultFall(List<ICoverage> faelle) {
		ICoverage ret = null;
		if (faelle != null) {
			for (ICoverage fall : faelle) {
				if ("online".equals(fall.getDescription())) {
					// is the only, or the newest online fall
					if (ret == null) {
						ret = fall;
					} else if (ret != null && (fall.getLastupdate().compareTo(ret.getLastupdate()) == 1)) {
						ret = fall;
					}
				}
			}
		}
		if (ret != null) {
			// make sure default fall is closed
			if (ret.getDateTo() == null) {
				closeFall(ret);
			}
		}
		return ret;
	}

	private Optional<ch.elexis.core.model.IEncounter> getIEncounter(IEncounter iEncounter) {
		String IEncountersId = iEncounter.getConsultationId();
		if (IEncountersId != null && !IEncountersId.isEmpty()) {
			return coreModelService.load(IEncountersId, ch.elexis.core.model.IEncounter.class);
		}
		return Optional.empty();
	}

	public Optional<IMandator> getPerformer(IEncounter iEncounter) {
		return coreModelService.load(iEncounter.getMandatorId(), IMandator.class);
	}

	public Optional<IPatient> getPatient(IEncounter iEncounter) {
		return coreModelService.load(iEncounter.getPatientId(), IPatient.class);
	}

	public Optional<String> getMandatorId(Encounter fhirObject) {
		List<EncounterParticipantComponent> participants = fhirObject.getParticipant();
		for (EncounterParticipantComponent encounterParticipantComponent : participants) {
			if (encounterParticipantComponent.hasIndividual()) {
				Reference reference = encounterParticipantComponent.getIndividual();
				if (FhirUtil.isReferenceType(reference, Practitioner.class.getSimpleName())) {
					return FhirUtil.getId(reference);
				}
			}
		}
		return Optional.empty();
	}

	public Optional<String> getPatientId(Encounter fhirObject) {
		return FhirUtil.getId(fhirObject.getSubject());
	}
}
