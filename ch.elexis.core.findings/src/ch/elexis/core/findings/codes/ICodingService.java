package ch.elexis.core.findings.codes;

import java.util.List;

import ch.elexis.core.findings.ICoding;

/**
 * The main entry point to load code systems. Different {@link ICodingContribution} implementations
 * provide access to the coding of different code systems. There should be 1 implementation of a
 * {@link ILocalCodingContribution} available, which can be used to create new codings.
 * 
 * @author thomas
 *
 */
public interface ICodingService {
	
	/**
	 * Get a list of all available code systems.
	 * 
	 * @return
	 */
	public List<String> getAvailableCodeSystems();
	
	/**
	 * Get a list of all available codes of the specified code system.
	 * 
	 * @param system
	 * @return
	 */
	public List<ICoding> getAvailableCodes(String system);
	
	/**
	 * Add a {@link ICoding} to the {@link ILocalCodingContribution} implementation.
	 * 
	 * @param coding
	 */
	public void addLocalCoding(ICoding coding);
	
	/**
	 * Remove a {@link ICoding} from the {@link ILocalCodingContribution} implementation.
	 * 
	 * @param coding
	 */
	public void removeLocalCoding(ICoding coding);
}
