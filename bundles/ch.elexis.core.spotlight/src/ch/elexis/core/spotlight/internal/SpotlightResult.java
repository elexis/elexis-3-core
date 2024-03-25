package ch.elexis.core.spotlight.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ch.elexis.core.spotlight.ISpotlightResult;
import ch.elexis.core.spotlight.ISpotlightResultEntry;
import ch.elexis.core.spotlight.ISpotlightResultEntry.Category;

public class SpotlightResult implements ISpotlightResult {

	private Set<ISpotlightResultEntry> patientEntries;
	private Set<ISpotlightResultEntry> encounterEntries;
	private Set<ISpotlightResultEntry> documentEntries;
	private Set<ISpotlightResultEntry> letterEntries;

	private Set<Category> usedCategories;

	public SpotlightResult() {
		patientEntries = Collections.synchronizedSet(new HashSet<>());
		encounterEntries = Collections.synchronizedSet(new HashSet<>());
		documentEntries = Collections.synchronizedSet(new HashSet<>());
		letterEntries = Collections.synchronizedSet(new HashSet<>());
		usedCategories = new HashSet<>(4);
	}

	@Override
	public void addEntry(Category category, String label, String storeToString, Object loadedObject) {
		ISpotlightResultEntry entry = new SpotlightResultEntry(category, label, storeToString, loadedObject);

		switch (category) {
		case PATIENT:
			patientEntries.add(entry);
			break;
		case DOCUMENT:
			documentEntries.add(entry);
			break;
		case ENCOUNTER:
			encounterEntries.add(entry);
			break;
		case LETTER:
			letterEntries.add(entry);
			break;
		default:
			break;
		}

	}

	@Override
	public void clear() {
		patientEntries.clear();
		encounterEntries.clear();
		documentEntries.clear();
		letterEntries.clear();
	}

	@Override
	public Set<Category> hasResultsIn() {
		usedCategories.clear();
		if (!patientEntries.isEmpty()) {
			usedCategories.add(Category.PATIENT);
		}
		if (!encounterEntries.isEmpty()) {
			usedCategories.add(Category.ENCOUNTER);
		}
		if (!documentEntries.isEmpty()) {
			usedCategories.add(Category.DOCUMENT);
		}
		if (!letterEntries.isEmpty()) {
			usedCategories.add(Category.LETTER);
		}
		return usedCategories;
	}

	@Override
	public Set<ISpotlightResultEntry> getResultPerCategory(Category category) {
		switch (category) {
		case PATIENT:
			return patientEntries;
		case ENCOUNTER:
			return encounterEntries;
		case DOCUMENT:
			return documentEntries;
		case LETTER:
			return letterEntries;
		default:
			break;
		}
		return Collections.emptySet();
	}

}
