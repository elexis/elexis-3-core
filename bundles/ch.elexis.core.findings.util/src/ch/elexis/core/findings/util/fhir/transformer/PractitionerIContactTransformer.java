package ch.elexis.core.findings.util.fhir.transformer;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Practitioner;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.transformer.helper.IPersonHelper;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.INamedQuery;
import ch.elexis.core.services.IUserService;
import ch.elexis.core.services.IXidService;

@Component(property = IFhirTransformer.TRANSFORMERID + "=Practitioner.IContact")
public class PractitionerIContactTransformer implements IFhirTransformer<Practitioner, IContact> {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;

	@Reference
	private IXidService xidService;

	@Reference
	private IUserService userService;

	private IPersonHelper personHelper;

	public PractitionerIContactTransformer() {
		personHelper = new IPersonHelper();
	}

	@Override
	public Optional<Practitioner> getFhirObject(IContact localObject, SummaryEnum summaryEnum, Set<Include> includes) {
		Practitioner practitioner = new Practitioner();

		practitioner.setId(new IdDt("Practitioner", localObject.getId(), Long.toString(localObject.getLastupdate())));

		List<Identifier> identifiers = personHelper.getIdentifiers(localObject, xidService);
		identifiers.add(getElexisObjectIdentifier(localObject));
		practitioner.setIdentifier(identifiers);

		if (localObject.isPerson()) {
			IPerson mandatorPerson = modelService.load(localObject.getId(), IPerson.class).get();
			practitioner.setName(personHelper.getHumanNames(mandatorPerson));
			practitioner.setGender(personHelper.getGender(mandatorPerson.getGender()));
			practitioner.setBirthDate(personHelper.getBirthDate(mandatorPerson));

			INamedQuery<IUser> query = modelService.getNamedQuery(IUser.class, "kontakt");
			List<IUser> usersLocal = query.executeWithParameters(query.getParameterMap("kontakt", mandatorPerson));
			if (!usersLocal.isEmpty()) {
				practitioner.setActive(usersLocal.get(0).isActive());
			}
		}

		practitioner.setAddress(personHelper.getAddresses(localObject));
		practitioner.setTelecom(personHelper.getContactPoints(localObject));

		return Optional.of(practitioner);
	}

	@Override
	public Optional<IContact> getLocalObject(Practitioner fhirObject) {
		String id = fhirObject.getIdElement().getIdPart();
		if (id != null && !id.isEmpty()) {
			return modelService.load(id, IContact.class);
		}
		return Optional.empty();
	}

	@Override
	public Optional<IContact> updateLocalObject(Practitioner fhirObject, IContact localObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<IContact> createLocalObject(Practitioner fhirObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Practitioner.class.equals(fhirClazz) && IContact.class.equals(localClazz);
	}
}
