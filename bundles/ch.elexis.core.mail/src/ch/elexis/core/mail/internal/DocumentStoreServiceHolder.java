package ch.elexis.core.mail.internal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.documents.DocumentStore;

@Component(service = {})
public class DocumentStoreServiceHolder {
	private static DocumentStore documentStore;
	
	@Reference
	public void bind(DocumentStore service){
		DocumentStoreServiceHolder.documentStore = service;
	}
	
	public static void unbind(DocumentStore service){
		DocumentStoreServiceHolder.documentStore = null;
	}
	
	public static DocumentStore getService(){
		return documentStore;
	}
}
