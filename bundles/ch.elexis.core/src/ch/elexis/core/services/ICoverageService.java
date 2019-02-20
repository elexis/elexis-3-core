package ch.elexis.core.services;

import ch.elexis.core.model.ICoverage;

public interface ICoverageService {
	
	/**
	 * Test if all the required fields of the {@link ICoverage} are set.
	 * 
	 * @param coverage
	 * @return
	 */
	public boolean isValid(ICoverage coverage);
}
