package ch.elexis.core.ui.documents.service;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.documents.DocumentStore;

@Component(service = {})
public class DocumentStoreServiceHolder {
	private static DocumentStore localDocumentStore;
	
	@Reference
	public void bind(DocumentStore service){
		DocumentStoreServiceHolder.localDocumentStore = service;
	}
	
	public void unbind(DocumentStore service){
		DocumentStoreServiceHolder.localDocumentStore = null;
	}
	
	public static DocumentStore getService(){
		return localDocumentStore;
	}
}
