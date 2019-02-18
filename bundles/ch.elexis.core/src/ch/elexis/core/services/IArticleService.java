package ch.elexis.core.services;

import java.util.Optional;

import ch.elexis.core.model.IArticle;

public interface IArticleService {
	
	/**
	 * Find any (i.e. of any type) article matching the provided gtin. The respective type search
	 * order is not determined, first match is returned.
	 * 
	 * @param gtin
	 * @return
	 */
	public Optional<? extends IArticle> findAnyByGTIN(String gtin);
	
}
