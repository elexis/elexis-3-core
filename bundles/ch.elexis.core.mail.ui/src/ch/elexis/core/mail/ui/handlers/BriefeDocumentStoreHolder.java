package ch.elexis.core.mail.ui.handlers;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IDocumentStore;

@Component(service = {})
public class BriefeDocumentStoreHolder {
	private static IDocumentStore briefeStore;
	
	@Reference(target = "(storeid=ch.elexis.data.store.brief)")
	public void bind(IDocumentStore service){
		BriefeDocumentStoreHolder.briefeStore = service;
	}
	
	public static void unbind(IDocumentStore service){
		BriefeDocumentStoreHolder.briefeStore = null;
	}
	
	public static IDocumentStore get(){
		return briefeStore;
	}
}
