package ch.elexis.core.findings.util.fhir.transformer;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Person;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.transformer.helper.IPersonHelper;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IUserService;
import ch.elexis.core.services.IXidService;

@Component
public class PersonIPersonTransformer implements IFhirTransformer<Person, IPerson> {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;

	@Reference
	private IXidService xidService;

	@Reference
	private IUserService userService;

	private IPersonHelper personHelper;

	public PersonIPersonTransformer() {
		personHelper = new IPersonHelper();
	}

	@Override
	public Optional<Person> getFhirObject(IPerson localObject, SummaryEnum summaryEnum, Set<Include> includes) {
		Person person = new Person();

		person.setId(new IdDt("Person", localObject.getId()));

		List<Identifier> identifiers = personHelper.getIdentifiers(localObject, xidService);
		identifiers.add(getElexisObjectIdentifier(localObject));
		person.setIdentifier(identifiers);

		person.setName(personHelper.getHumanNames(localObject));
		List<Address> addresses = personHelper.getAddresses(localObject);
		for (Address address : addresses) {
			person.addAddress(address);
		}
		List<ContactPoint> contactPoints = personHelper.getContactPoints(localObject);
		for (ContactPoint contactPoint : contactPoints) {
			person.addTelecom(contactPoint);
		}

		return Optional.of(person);
	}

	@Override
	public Optional<IPerson> getLocalObject(Person fhirObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<IPerson> updateLocalObject(Person fhirObject, IPerson localObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<IPerson> createLocalObject(Person fhirObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Person.class.equals(fhirClazz) && IPerson.class.equals(localClazz);
	}

}
