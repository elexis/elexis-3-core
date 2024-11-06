package ch.elexis.core.services;

import java.time.LocalDate;
import java.util.List;

import ch.elexis.core.model.IPerson;
import ch.elexis.core.types.Gender;

public interface IContactService {

	/**
	 * Find possible duplicates of a person. Must provide exact date of birth and
	 * gender. Default maximum distance for matching is 4.
	 * 
	 * @param dateOfBirth    required
	 * @param gender         required
	 * @param familyName     fuzzy matched
	 * @param firstName      fuzzy matched
	 * @param includeDeleted
	 * @return
	 */
	public default List<IPerson> findPersonFuzzy(LocalDate dateOfBirth, Gender gender, String familyName,
			String firstName, boolean includeDeleted) {
		return findPersonFuzzy(dateOfBirth, gender, familyName, firstName, 4, includeDeleted);
	}

	/**
	 * Find possible duplicates of a person. Must provide exact date of birth and
	 * gender.
	 * 
	 * @param dateOfBirth
	 * @param gender
	 * @param familyName
	 * @param firstName
	 * @param maxDistance
	 * @param includeDeleted
	 * @return
	 */
	public List<IPerson> findPersonFuzzy(LocalDate dateOfBirth, Gender gender, String familyName, String firstName,
			int maxDistance, boolean includeDeleted);
}
