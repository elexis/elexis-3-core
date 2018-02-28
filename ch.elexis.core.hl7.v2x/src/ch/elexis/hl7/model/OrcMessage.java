package ch.elexis.hl7.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents the ORC message from a hl7 file
 * 
 * @author med1
 *
 */
public class OrcMessage {
	private Set<String> names = new HashSet<>();
	
	/**
	 * Adds a new name attribute
	 * 
	 * @param name
	 */
	public void addName(String name){
		if (name != null) {
			names.add(name);
		}
	}
	
	/**
	 * Returns all added names
	 * 
	 * @return
	 */
	public Set<String> getNames(){
		return names;
	}
}
