package ch.elexis.core.findings.util.fhir.transformer;

import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.r4.model.Person;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.fhir.mapper.r4.IPersonPersonAttributeMapper;
import ch.elexis.core.fhir.mapper.r4.util.FhirUtil;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IUserService;
import ch.elexis.core.services.IXidService;

@Component(property = IFhirTransformer.TRANSFORMERID + "=Person.IPerson")
public class PersonIPersonTransformer implements IFhirTransformer<Person, IPerson> {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;

	@Reference
	private IXidService xidService;

	@Reference
	private IUserService userService;

	private IPersonPersonAttributeMapper attributeMapper;

	@Activate
	private void activate() {
		attributeMapper = new IPersonPersonAttributeMapper(modelService, xidService);
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Person.class.equals(fhirClazz) && IPerson.class.equals(localClazz);
	}

	@Override
	public Optional<Person> getFhirObject(IPerson localObject, SummaryEnum summaryEnum, Set<Include> includes) {
		Person person = new Person();
		attributeMapper.elexisToFhir(localObject, person, summaryEnum);
		return Optional.of(person);
	}

	@Override
	public Optional<IPerson> getLocalObject(Person fhirObject) {
		if (fhirObject != null && fhirObject.getId() != null) {
			Optional<String> localId = FhirUtil.getLocalId(fhirObject.getId());
			if (localId.isPresent()) {
				return modelService.load(localId.get(), IPerson.class);
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<? extends Identifiable> getLocalObjectForReference(String fhirReference) {
		if (fhirReference.startsWith(Person.class.getSimpleName())) {
			Optional<String> localId = FhirUtil.getLocalId(fhirReference);
			if (localId.isPresent()) {
				return modelService.load(localId.get(), IPerson.class);
			}
		}
		return IFhirTransformer.super.getLocalObjectForReference(fhirReference);
	}

	@Override
	public Optional<IPerson> updateLocalObject(Person fhirObject, IPerson localObject) {
		attributeMapper.fhirToElexis(fhirObject, localObject);
		modelService.save(localObject);
		return Optional.of(localObject);
	}

	@Override
	public Optional<IPerson> createLocalObject(Person fhirObject) {
		IPerson create = modelService.create(IPerson.class);
		attributeMapper.fhirToElexis(fhirObject, create);
		modelService.save(create);
		return Optional.of(create);
	}

}
