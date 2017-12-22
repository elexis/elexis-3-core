package ch.elexis.core.ui.stock.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IArticleService;
import ch.elexis.core.ui.stock.service.internal.ArticleServiceStore;

@Component(service = {})
public class ArticleServiceHolder {
	private static ArticleServiceStore articleServiceStore;
	
	@Reference(unbind = "-")
	public static void bind(ArticleServiceStore service){
		ArticleServiceHolder.articleServiceStore = service;
	}
	
	/**
	 * Returns a {@link IArticleService} by store id
	 * 
	 * @param storeId
	 * @return
	 */
	public static Optional<IArticleService> getService(String storeId){
		IArticleService articleService = articleServiceStore.getServices().get(storeId);
		if (articleService != null) {
			return Optional.of(articleService);
		}
		return Optional.empty();
	}
	
	/**
	 * Returns all registered store ids for {@link IArticleService}
	 * 
	 * @return
	 */
	public static List<String> getStoreIds(){
		List<String> storeIds = new ArrayList<>(articleServiceStore.getServices().keySet());
		Collections.sort(storeIds);
		return storeIds;
	}
}
