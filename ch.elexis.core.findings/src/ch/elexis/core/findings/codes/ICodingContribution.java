package ch.elexis.core.findings.codes;

import java.util.List;
import java.util.Optional;

import ch.elexis.core.findings.ICoding;

/**
 * Definition of a code system contribution to the {@link ICodingService}.
 * 
 * @author thomas
 *
 */
public interface ICodingContribution {
	
	/**
	 * Get the defining URL of the code system.
	 * 
	 * @return
	 */
	public String getCodeSystem();
	
	/**
	 * Return all codes contained in the code system.
	 * 
	 * @return
	 */
	public List<ICoding> getCodes();
	
	/**
	 * Return a matching {@link ICoding} instance for the requested code, or empty if there is no
	 * such code.
	 * 
	 * @param code
	 * @return
	 */
	public Optional<ICoding> getCode(String code);
}
