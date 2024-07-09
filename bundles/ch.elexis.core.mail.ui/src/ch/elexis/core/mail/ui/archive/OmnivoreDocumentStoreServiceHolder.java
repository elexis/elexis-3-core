package ch.elexis.core.mail.ui.archive;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IDocumentStore;

@Component
public class OmnivoreDocumentStoreServiceHolder {

	private static IDocumentStore documentStore;

	@Reference(target = "(storeid=ch.elexis.data.store.omnivore)")
	public void setDocumentStore(IDocumentStore documentStore) {
		OmnivoreDocumentStoreServiceHolder.documentStore = documentStore;
	}

	public static IDocumentStore get() {
		if (documentStore == null) {
			throw new IllegalStateException("No IDocumentStore available");
		}
		return documentStore;
	}

	public static boolean isAvailable() {
		return documentStore != null;
	}
}
