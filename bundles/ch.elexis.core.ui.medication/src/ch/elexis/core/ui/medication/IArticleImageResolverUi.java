package ch.elexis.core.ui.medication;

import org.eclipse.swt.graphics.Image;

import ch.elexis.core.model.IArticle;

/**
 * UI service to resolve the marking {@link Image} (e.g. P/SL/nonPharma/blackbox)
 * of an {@link IArticle} as shown in the article list (Artikelstamm). Implemented
 * by an article source bundle (e.g. Artikelstamm) and consumed by the medication
 * list to display the same article markings.
 */
public interface IArticleImageResolverUi {

	/**
	 * Get the marking {@link Image} for the provided {@link IArticle}, or
	 * <code>null</code> if no marking applies (e.g. the article does not originate
	 * from a source known to this resolver).
	 *
	 * @param article
	 * @return the image or <code>null</code>
	 */
	public Image getImage(IArticle article);
}
