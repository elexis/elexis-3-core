package ch.elexis.core.findings.util.fhir.transformer;

import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.r4.model.Encounter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.fhir.mapper.r4.IEncounterEncounterAttributeMapper;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.services.IEncounterService;
import jakarta.enterprise.context.Dependent;

@Dependent
@Component
public class EncounterModelIEncounterTransformer implements IFhirTransformer<Encounter, IEncounter> {

	@Reference
	IEncounterService encounterService;

	private IEncounterEncounterAttributeMapper attributeMapper = new IEncounterEncounterAttributeMapper(
			encounterService);

	@Override
	public Optional<Encounter> getFhirObject(IEncounter localObject, SummaryEnum summaryEnum, Set<Include> includes) {
		Encounter encounter = new Encounter();
		attributeMapper.elexisToFhir(localObject, encounter, summaryEnum);
		return Optional.of(encounter);
	}

	@Override
	public Optional<IEncounter> getLocalObject(Encounter fhirObject) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<IEncounter> updateLocalObject(Encounter fhirObject, IEncounter localObject) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<IEncounter> createLocalObject(Encounter fhirObject) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Encounter.class.equals(fhirClazz) && IEncounter.class.equals(localClazz);
	}

}
