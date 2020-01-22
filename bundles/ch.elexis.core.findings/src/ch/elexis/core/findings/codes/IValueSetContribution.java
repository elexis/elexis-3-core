package ch.elexis.core.findings.codes;

import java.util.List;

import ch.elexis.core.findings.ICoding;

public interface IValueSetContribution {
	
	/**
	 * Get a list of all ValueSet ids of the {@link IValueSetContribution}.
	 * 
	 * @return
	 */
	public List<String> getValueSetIds();
	
	/**
	 * Get a list of all ValueSet names of the {@link IValueSetContribution}.
	 * 
	 * @return
	 */
	public List<String> getValueSetNames();
	
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
