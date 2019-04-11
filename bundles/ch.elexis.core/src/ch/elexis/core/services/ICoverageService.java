package ch.elexis.core.services;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;

public interface ICoverageService {
	
	public static enum Tiers {
			PAYANT, GARANT
	};
	
	/**
	 * Test if all the required fields of the {@link ICoverage} are set.
	 * 
	 * @param coverage
	 * @return
	 */
	public boolean isValid(ICoverage coverage);
	
	/**
	 * Retrieve a required String Value from this billing system's definition. If no variable with
	 * that name is found, the billings system constants will be searched
	 * 
	 * @param name
	 * @return a string that might be empty but will never be null.
	 */
	public String getRequiredString(ICoverage coverage, String name);
	
	public void setRequiredString(ICoverage coverage, String name, String value);
	
	/**
	 * Retrieve a required {@link IContact} from this Fall's Billing system's requirements
	 * 
	 * @param name
	 *            the requested Kontakt's name
	 * @return the {@link IContact} or null if no such {@link IContact} was found
	 */
	public IContact getRequiredContact(ICoverage coverage, String name);
	
	public void setRequiredContact(ICoverage coverage, String name, IContact value);
	
	public Tiers getTiersType(ICoverage coverage);
	
	public boolean getCopyForPatient(ICoverage coverage);
	
	public void setCopyForPatient(ICoverage coverage, boolean copy);
	
	public String getDefaultCoverageLabel();
	
	public String getDefaultCoverageReason();
	
	public String getDefaultCoverageLaw();
}
