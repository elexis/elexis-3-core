package ch.elexis.core.ui.stock.service.internal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import ch.elexis.core.services.IArticleService;

/**
 * Central store were all article services are registered
 *
 */
@Component(service = ArticleServiceStore.class)
public class ArticleServiceStore {
	
	private final ConcurrentMap<String, IArticleService> services = new ConcurrentHashMap<>();
	
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	void addArticleService(IArticleService store){
		services.put(store.getClass().getName(), store);
	}
	
	void removeArticleService(IArticleService store){
		services.remove(store.getClass().getName());
	}
	
	public ConcurrentMap<String, IArticleService> getServices(){
		return services;
	}
}
