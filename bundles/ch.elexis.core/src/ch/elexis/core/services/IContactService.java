package ch.elexis.core.services;

import java.time.LocalDate;
import java.util.List;

import ch.elexis.core.model.IPerson;
import ch.elexis.core.types.Gender;

public interface IContactService {

	/**
	 * Find possible duplicates of a person. Must provide exact date of birth and
	 * gender
	 * 
	 * @param dateOfBirth    required
	 * @param gender         required
	 * @param familyName     fuzzy matched
	 * @param firstName      fuzzy matched
	 * @param includeDeleted
	 * @return
	 */
	public List<IPerson> findPersonDuplicates(LocalDate dateOfBirth, Gender gender, String familyName, String firstName,
			boolean includeDeleted);

}
