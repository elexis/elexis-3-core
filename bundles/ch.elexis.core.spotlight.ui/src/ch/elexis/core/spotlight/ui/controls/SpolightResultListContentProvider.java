package ch.elexis.core.spotlight.ui.controls;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredContentProvider;

import ch.elexis.core.spotlight.ISpotlightResult;
import ch.elexis.core.spotlight.ISpotlightResultEntry.Category;

public class SpolightResultListContentProvider implements IStructuredContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		ISpotlightResult result = (ISpotlightResult) inputElement;

		Set<Object> results = new LinkedHashSet<>();
		Set<Category> hasResultsIn = result.hasResultsIn();
		// Patients always first in list
		if (hasResultsIn.contains(Category.PATIENT)) {
			results.add(Category.PATIENT);
			results.addAll(result.getResultPerCategory(Category.PATIENT));
		}
		hasResultsIn.remove(Category.PATIENT);
		for (Category category : hasResultsIn) {
			results.add(category);
			results.addAll(result.getResultPerCategory(category));
		}
		return results.toArray();
	}

}
