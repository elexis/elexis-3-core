package ch.elexis.core.services.holder;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.services.IDocumentStore;

public class BriefDocumentStoreHolder {

	public static IDocumentStore get() {
		return PortableServiceLoader.getService(IDocumentStore.class, "(storeid=ch.elexis.data.store.brief)").get();
	}
}
