package ch.elexis.core.services;

import java.util.List;
import java.util.Optional;

import ch.elexis.core.model.IArticle;

/**
 * Provides details of an article that are not part of the local article data,
 * like the picture of the dose form, the unit used for prescribing and the
 * contained substances. Implementing this service requires access to an article
 * information provider, and is therefore optional.
 */
public interface IMedicationDetailService {

	/**
	 * Get the picture of the dose form of the article as JPEG.
	 *
	 * @param article
	 * @return the picture, or empty if the article has none
	 */
	public Optional<byte[]> getImage(IArticle article);

	/**
	 * Get the unit the article is prescribed in, e.g. Stk.
	 *
	 * @param article
	 * @return the unit, or empty if it is not known
	 */
	public Optional<String> getPrescriptionUnit(IArticle article);

	/**
	 * Get the substances the article contains.
	 *
	 * @param article
	 * @return the substances, empty if they are not known
	 */
	public List<String> getSubstances(IArticle article);

	/**
	 * Load the details of the articles into the cache of the implementation.
	 * Called once before a series of {@link #getImage(IArticle)},
	 * {@link #getPrescriptionUnit(IArticle)} and {@link #getSubstances(IArticle)}
	 * calls, so the details do not have to be loaded one after the other.
	 *
	 * @param articles
	 */
	public default void loadDetails(List<IArticle> articles) {
	}
}
