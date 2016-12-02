package ch.elexis.core.findings.codes;

import java.util.List;
import java.util.Optional;

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
	 * Return a matching {@link ICoding} instance for the requested code of the system, or empty if
	 * there is no such code.
	 * 
	 * @param code
	 * @return
	 */
	public Optional<ICoding> getCode(String system, String code);
	
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

	/**
	 * Get a label for a {@link ICoding} object. The code system is represented
	 * with the last part of the system uri. <br />
	 * Examples: <br />
	 * icd G21.0 Malignant neuroleptic syndrome<br />
	 * icpc-2 K02 Druck/Engegefühl des Herzens
	 * 
	 * @param iCoding
	 * @return
	 */
	public String getLabel(ICoding iCoding);

	/**
	 * Get a short label for a {@link ICoding} object. Only codesystem and code
	 * represented. The code system is represented with the last part of the
	 * system uri. <br />
	 * Examples: <br />
	 * icd:G21.0 <br />
	 * icpc-2:K02
	 * 
	 * @param iCoding
	 * @return
	 */
	public String getShortLabel(ICoding iCoding);

}
