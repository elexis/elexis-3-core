package ch.elexis.core.coding.internal.model;

import java.util.List;

public class ValueSet {
	public String ident;
	public String id;
	public List<ConceptList> conceptList;
	
	@Override
	public String toString(){
		return "ValueSet [ident=" + ident + ", id=" + id + ", conceptList=" + conceptList + "]";
	}
	
}
