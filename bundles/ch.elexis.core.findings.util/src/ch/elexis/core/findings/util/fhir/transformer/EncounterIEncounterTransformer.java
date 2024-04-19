package ch.elexis.core.findings.util.fhir.transformer;

import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Encounter;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.migration.IMigratorService;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.transformer.helper.AbstractHelper;
import ch.elexis.core.findings.util.fhir.transformer.helper.FhirUtil;
import ch.elexis.core.findings.util.fhir.transformer.helper.FindingsContentHelper;
import ch.elexis.core.findings.util.fhir.transformer.helper.IEncounterHelper;
import ch.elexis.core.findings.util.fhir.transformer.mapper.IEncounterEncounterAttributeMapper;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IModelService;

@Component
public class EncounterIEncounterTransformer implements IFhirTransformer<Encounter, IEncounter> {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.findings.model)")
	private IModelService findingsModelService;

	@Reference
	private IFindingsService findingsService;

	@Reference
	private IMigratorService migratorService;

	private FindingsContentHelper contentHelper;
	private IEncounterHelper encounterHelper;
	private IEncounterEncounterAttributeMapper attributeMapper;

	@Activate
	public void activate() {
		contentHelper = new FindingsContentHelper();
		encounterHelper = new IEncounterHelper(coreModelService, findingsModelService);
		attributeMapper = new IEncounterEncounterAttributeMapper();
	}

	@Override
	public Optional<Encounter> getFhirObject(IEncounter localObject, SummaryEnum summaryEnum, Set<Include> includes) {
		Optional<IBaseResource> resource = contentHelper.getResource(localObject);
		if (resource.isPresent()) {
			return Optional.of((Encounter) resource.get());
		}
		return Optional.empty();
	}

	@Override
	public Optional<IEncounter> getLocalObject(Encounter fhirObject) {
		if (fhirObject != null && fhirObject.getId() != null) {
			Optional<String> localId = FhirUtil.getLocalId(fhirObject.getId());
			if (localId.isPresent()) {
				return findingsService.findById(localId.get(), IEncounter.class);
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<IEncounter> updateLocalObject(Encounter fhirObject, IEncounter localObject) {
		Optional<ch.elexis.core.model.IEncounter> behandlung = coreModelService.load(localObject.getConsultationId(),
				ch.elexis.core.model.IEncounter.class);
		behandlung.ifPresent(cons -> {
			attributeMapper.fhirToElexis(fhirObject, cons);
			migratorService.migrateConsultationsFindings(localObject.getConsultationId(), IEncounter.class);
		});
		return Optional.empty();
	}

	@Override
	public Optional<IEncounter> createLocalObject(Encounter fhirObject) {
		// patient and performer must be present
		Optional<IMandator> performerKontakt = coreModelService.load(encounterHelper.getMandatorId(fhirObject).get(),
				IMandator.class);
		Optional<IPatient> patientKontakt = coreModelService.load(encounterHelper.getPatientId(fhirObject).get(),
				IPatient.class);
		if (performerKontakt.isPresent() && patientKontakt.isPresent()) {
			IEncounter iEncounter = findingsService.create(IEncounter.class);
			contentHelper.setResource(fhirObject, iEncounter);
			patientKontakt.ifPresent(k -> iEncounter.setPatientId(k.getId()));
			performerKontakt.ifPresent(k -> iEncounter.setMandatorId(k.getId()));
			encounterHelper.createIEncounter(iEncounter).ifPresent(cons -> {
				iEncounter.setConsultationId(cons.getId());
				attributeMapper.fhirToElexis(fhirObject, cons);
				coreModelService.save(cons);
				AbstractHelper.acquireAndReleaseLock(cons);
			});
			findingsService.saveFinding(iEncounter);
			return Optional.of(iEncounter);
		} else {
			LoggerFactory.getLogger(EncounterIEncounterTransformer.class)
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
