package ch.elexis.core.coding.internal.model;

import java.util.List;

public class ValueSets {
	
	public List<ValueSetList> valueSets;
	
	public boolean hasValueSet(){
		return valueSets != null && !valueSets.isEmpty() && valueSets.get(0).valueSet != null
			&& !valueSets.get(0).valueSet.isEmpty();
	}
	
	@Override
	public String toString(){
		return "ValueSets [valueSets=" + valueSets + "]";
	}
	
}
