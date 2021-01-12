package ch.elexis.core.services.holder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IDocumentStore;

@Component
public class BriefDocumentStoreHolder {
	
	private static IDocumentStore briefDocumentStore;
	
	@Reference(target = "(storeid=ch.elexis.data.store.brief)")
	public void setBriefDocumentStore(IDocumentStore briefDocumentStore){
		BriefDocumentStoreHolder.briefDocumentStore = briefDocumentStore;
	}
	
	public static IDocumentStore get(){
		return briefDocumentStore;
	}
}
