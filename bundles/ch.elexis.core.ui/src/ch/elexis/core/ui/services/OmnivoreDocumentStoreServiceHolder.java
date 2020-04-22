package ch.elexis.core.ui.services;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IDocumentStore;

@Component(service = {})
public class OmnivoreDocumentStoreServiceHolder {
	
	private static IDocumentStore documentStore;
	
	@Reference(target = "(storeid=ch.elexis.data.store.omnivore)")
	public void setDocumentStore(IDocumentStore documentStore){
		OmnivoreDocumentStoreServiceHolder.documentStore = documentStore;
	}
	
	public static Optional<IDocumentStore> get(){
		return Optional.of(documentStore);
	}
	
}
