package ch.elexis.core.findings.codes;

import ch.elexis.core.findings.ICoding;

/**
 * Definition of a local code system that can be used to manage local coding.
 * 
 * @author thomas
 *
 */
public interface ILocalCodingContribution extends ICodingContribution {
	
	public static final String LOCAL_CODE_SYSTEM = "www.elexis.info/coding/local";

	/**
	 * Add a {@link ICoding} to the code system.
	 * 
	 * @param coding
	 */
	public void addCoding(ICoding coding);
	
	/**
	 * Remove a {@link ICoding} from the code system.
	 * 
	 * @param coding
	 */
	public void removeCoding(ICoding coding);
}
