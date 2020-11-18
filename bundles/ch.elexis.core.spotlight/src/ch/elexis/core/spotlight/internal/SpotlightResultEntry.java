package ch.elexis.core.spotlight.internal;

import ch.elexis.core.spotlight.ISpotlightResultEntry;

public class SpotlightResultEntry implements ISpotlightResultEntry {
	
	final Category category;
	final String label;
	final String storeToString;
	
	public SpotlightResultEntry(Category category, String label, String storeToString,
		String iconUri){
		this.category = category;
		this.label = label;
		this.storeToString = storeToString;
	}
	
	@Override
	public Category getCategory(){
		return category;
	}
	
	@Override
	public String getLabel(){
		return label;
	}
	
	@Override
	public String getIdentifierString(){
		return storeToString;
	}
	
	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((category == null) ? 0 : category.hashCode());
		result = prime * result + ((storeToString == null) ? 0 : storeToString.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj){
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SpotlightResultEntry other = (SpotlightResultEntry) obj;
		if (category != other.category)
			return false;
		if (storeToString == null) {
			if (other.storeToString != null)
				return false;
		} else if (!storeToString.equals(other.storeToString))
			return false;
		return true;
	}
	
}
