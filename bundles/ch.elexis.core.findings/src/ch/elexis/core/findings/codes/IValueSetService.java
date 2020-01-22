package ch.elexis.core.findings.codes;

import java.util.List;

import ch.elexis.core.findings.ICoding;

/**
 * The main entry point to load value sets. Different {@link IValueSetContribution} implementations
 * provide access to the coding of different value sets. It is not required that all codes of value
 * sets can also accessible via {@link ICodingService}.
 * 
 * @author thomas
 *
 */
public interface IValueSetService {
	
	/**
	 * Get all codes of the value set. Lookup is performed using the id.
	 * 
	 * @param id
	 * @return
	 */
	public List<ICoding> getValueSet(String id);
	
	/**
	 * Get all codes of the value set. Lookup is performed using the name.
	 * 
	 * @param name
	 * @return
	 */
	public List<ICoding> getValueSetByName(String name);
	
}
