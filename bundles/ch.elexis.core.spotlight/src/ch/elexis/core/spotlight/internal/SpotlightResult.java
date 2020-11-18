package ch.elexis.core.spotlight.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ch.elexis.core.spotlight.ISpotlightResult;
import ch.elexis.core.spotlight.ISpotlightResultEntry;
import ch.elexis.core.spotlight.ISpotlightResultEntry.Category;

public class SpotlightResult implements ISpotlightResult {
	
	private Set<ISpotlightResultEntry> patientEntries;
	
	public SpotlightResult(){
		patientEntries = Collections.synchronizedSet(new HashSet<>());
	}
	
	@Override
	public void addEntry(Category category, String label, String storeToString, String iconUri){
		ISpotlightResultEntry entry =
			new SpotlightResultEntry(category, label, storeToString, iconUri);
		
		switch (category) {
		case PATIENT:
			patientEntries.add(entry);
			break;
		case DOCUMENT:
			
			break;
		case ENCOUNTER:
			
			break;
		case LETTER:
			
			break;
		default:
			break;
		}
		
	}
	
	@Override
	public void clear(){
		patientEntries.clear();
	}
	
	@Override
	public Set<Category> hasResultsIn(){
		Set<Category> usedCategories = new HashSet<ISpotlightResultEntry.Category>(4);
		if (patientEntries.size() > 0) {
			usedCategories.add(Category.PATIENT);
		}
		return usedCategories;
	}
	
	@Override
	public Set<ISpotlightResultEntry> getResultPerCategory(Category category){
		switch (category) {
		case PATIENT:
			return patientEntries;
		
		default:
			break;
		}
		return Collections.emptySet();
	}
	
}
