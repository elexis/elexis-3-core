package ch.elexis.core.spotlight;

import java.util.Set;

import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.spotlight.ISpotlightResultEntry.Category;

/**
 * The result of an executed spotlight search.
 */
public interface ISpotlightResult {

	void clear();

	/**
	 *
	 * @param category         this entry belongs to
	 * @param label            to show for the result
	 * @param identifierString info on how to load this object, differs by category
	 */
	void addEntry(Category category, String label, String identifierString, @Nullable Object loadedObject);

	Set<Category> hasResultsIn();

	Set<ISpotlightResultEntry> getResultPerCategory(Category category);
}
