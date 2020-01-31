package ch.elexis.core.findings;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IDocumentStore;

@Component
public class DocumentStoreComponent {
	private static IDocumentStore documentStore;
	
	@Reference(target = "(storeid=ch.elexis.data.store.brief)")
	public void setIDocumentStore(IDocumentStore documentStore){
		DocumentStoreComponent.documentStore = documentStore;
	}
	
	public void unsetIDocumentStore(IDocumentStore documentStore){
		DocumentStoreComponent.documentStore = null;
	}
	
	public static IDocumentStore getService(){
		if (documentStore == null) {
			throw new IllegalStateException("No IDocumentStore set");
		}
		return documentStore;
	}
}
