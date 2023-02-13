package ch.elexis.core.findings.util.fhir.transformer;

import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.r4.model.RelatedPerson;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.transformer.mapper.IPersonRelatedPersonAttributeMapper;
import ch.elexis.core.model.IPerson;

@Component
public class RelatedPersonIPersonTransformer implements IFhirTransformer<RelatedPerson, IPerson> {

	private IPersonRelatedPersonAttributeMapper attributeMapper;

	@Activate
	private void activate() {
		attributeMapper = new IPersonRelatedPersonAttributeMapper();
	}

	@Override
	public Optional<RelatedPerson> getFhirObject(IPerson localObject, SummaryEnum summaryEnum, Set<Include> includes) {
		return Optional.empty();
	}

	@Override
	public Optional<IPerson> getLocalObject(RelatedPerson fhirObject) {
		return Optional.empty();
	}

	@Override
	public Optional<IPerson> updateLocalObject(RelatedPerson fhirObject, IPerson localObject) {
		attributeMapper.fhirToElexis(fhirObject, localObject);
		return Optional.of(localObject);
	}

	@Override
	public Optional<IPerson> createLocalObject(RelatedPerson fhirObject) {
		return Optional.empty();
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return RelatedPerson.class.equals(fhirClazz) && IPerson.class.equals(localClazz);
	}

}
