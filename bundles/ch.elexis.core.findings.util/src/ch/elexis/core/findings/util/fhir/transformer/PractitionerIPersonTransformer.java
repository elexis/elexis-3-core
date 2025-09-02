package ch.elexis.core.findings.util.fhir.transformer;

import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.r4.model.Practitioner;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.transformer.mapper.IPersonPractitionerAttributeMapper;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IXidService;

/**
 * A person who is directly or indirectly involved in the provisioning of
 * healthcare.
 * 
 * @see https://hl7.org/fhir/R4B/practitioner.html
 */
@Component(property = IFhirTransformer.TRANSFORMERID + "=Practitioner.IPerson")
public class PractitionerIPersonTransformer implements IFhirTransformer<Practitioner, IPerson> {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;

	@Reference
	private IXidService xidService;

	private IPersonPractitionerAttributeMapper attributeMapper;

	public PractitionerIPersonTransformer() {
		attributeMapper = new IPersonPractitionerAttributeMapper(xidService);
	}

	@Override
	public Optional<Practitioner> getFhirObject(IPerson localObject, SummaryEnum summaryEnum, Set<Include> includes) {
		Practitioner practitioner = new Practitioner();
		attributeMapper.elexisToFhir(localObject, practitioner, summaryEnum, includes);
		return Optional.of(practitioner);
	}

	@Override
	public Optional<IPerson> getLocalObject(Practitioner fhirObject) {
		String id = fhirObject.getIdElement().getIdPart();
		if (id != null && !id.isEmpty()) {
			return modelService.load(id, IPerson.class);
		}
		return Optional.empty();
	}

	@Override
	public Optional<IPerson> updateLocalObject(Practitioner fhirObject, IPerson localObject) {
		attributeMapper.fhirToElexis(fhirObject, localObject);
		modelService.save(localObject);
		return Optional.of(localObject);
	}

	@Override
	public Optional<IPerson> createLocalObject(Practitioner fhirObject) {
		IPerson mandator = modelService.create(IPerson.class);
		mandator.setMandator(true);
		attributeMapper.fhirToElexis(fhirObject, mandator);
		modelService.save(mandator);
		return Optional.of(mandator);
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Practitioner.class.equals(fhirClazz) && IPerson.class.equals(localClazz);
	}
}
