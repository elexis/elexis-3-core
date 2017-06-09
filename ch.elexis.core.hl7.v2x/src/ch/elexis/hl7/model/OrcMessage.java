package ch.elexis.hl7.model;

import java.util.HashSet;
import java.util.Set;

public class OrcMessage {
	private Set<String> names = new HashSet<>();
	
	public void addName(String name){
		if (name != null) {
			names.add(name);
		}
	}
	
	public Set<String> getNames(){
		return names;
	}
}
