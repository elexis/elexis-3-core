package ch.elexis.core.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.types.Gender;

@Component
public class ContactService implements IContactService {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@Override
	public List<IPerson> findPersonFuzzy(LocalDate dateOfBirth, Gender gender, String lastName, String firstName,
			int maxDistance, boolean includeDeleted) {

		IQuery<IPerson> query = coreModelService.getQuery(IPerson.class, includeDeleted);
		query.and(ModelPackage.Literals.IPERSON__DATE_OF_BIRTH, COMPARATOR.EQUALS, dateOfBirth);
		query.and(ModelPackage.Literals.IPERSON__GENDER, COMPARATOR.EQUALS, gender);
		List<IPerson> foundWithDateAndGender = query.execute();
		List<IPerson> found = new ArrayList<>();
		foundWithDateAndGender.forEach(person -> {
			// Levenshtein
			Integer firstNameDistance = Integer.MAX_VALUE;
			Integer lastNameDistance = Integer.MAX_VALUE;
			if (StringUtils.isNotBlank(firstName)) {
				firstNameDistance = LevenshteinDistance.getDefaultInstance().apply(person.getFirstName(), firstName);
			}
			if (StringUtils.isNotBlank(lastName)) {
				lastNameDistance = LevenshteinDistance.getDefaultInstance().apply(person.getLastName(), lastName);
			}
			if (firstNameDistance < maxDistance && lastNameDistance < maxDistance) {
				found.add(person);
			}
			// Cologne?
		});

		return found;
	}

}
