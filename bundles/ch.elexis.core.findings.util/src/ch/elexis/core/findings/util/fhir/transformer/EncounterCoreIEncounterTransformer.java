package ch.elexis.core.findings.util.fhir.transformer;

import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.r4.model.Encounter;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.transformer.helper.AbstractHelper;
import ch.elexis.core.findings.util.fhir.transformer.helper.FhirUtil;
import ch.elexis.core.findings.util.fhir.transformer.helper.IEncounterHelper;
import ch.elexis.core.findings.util.fhir.transformer.mapper.IEncounterEncounterAttributeMapper;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.builder.IEncounterBuilder;
import ch.elexis.core.services.IModelService;

@Component
public class EncounterCoreIEncounterTransformer implements IFhirTransformer<Encounter, IEncounter> {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;

	private IEncounterEncounterAttributeMapper attributeMapper;
	private IEncounterHelper encounterHelper;

	@Activate
	public void activate() {
		attributeMapper = new IEncounterEncounterAttributeMapper();
		encounterHelper = new IEncounterHelper(coreModelService, null);
	}

	@Override
	public Optional<Encounter> getFhirObject(IEncounter localObject, SummaryEnum summaryEnum, Set<Include> includes) {
		Encounter encounter = new Encounter();
		attributeMapper.elexisToFhir(localObject, encounter, summaryEnum, includes);
		return Optional.of(encounter);

	}

	@Override
	public Optional<IEncounter> getLocalObject(Encounter fhirObject) {
		if (fhirObject != null && fhirObject.getId() != null) {
			Optional<String> localId = FhirUtil.getLocalId(fhirObject.getId());
			if (localId.isPresent()) {
				return coreModelService.load(localId.get(), IEncounter.class);
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<IEncounter> updateLocalObject(Encounter fhirObject, IEncounter localObject) {
			attributeMapper.fhirToElexis(fhirObject, localObject);
			coreModelService.save(localObject);
			return Optional.of(localObject);
	}

	@Override
	public Optional<IEncounter> createLocalObject(Encounter fhirObject) {
		// patient and performer must be present
		Optional<IMandator> performerKontakt = coreModelService.load(encounterHelper.getMandatorId(fhirObject).get(),
				IMandator.class);
		Optional<IPatient> patientKontakt = coreModelService.load(encounterHelper.getPatientId(fhirObject).get(),
				IPatient.class);
		if (performerKontakt.isPresent() && patientKontakt.isPresent()) {
			ICoverage fall = encounterHelper.getOrCreateDefaultFall(patientKontakt.get());

			IEncounter iEncounter = new IEncounterBuilder(coreModelService, fall, performerKontakt.get()).build();
			attributeMapper.fhirToElexis(fhirObject, iEncounter);
			coreModelService.save(iEncounter);
			AbstractHelper.acquireAndReleaseLock(iEncounter);
			return Optional.of(iEncounter);
		} else {
			LoggerFactory.getLogger(EncounterCoreIEncounterTransformer.class)
					.warn("Could not create encounter for mandator [" + performerKontakt + "] patient ["
							+ patientKontakt + "]");
		}
		return Optional.empty();
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Encounter.class.equals(fhirClazz) && IEncounter.class.equals(localClazz);
	}
}
