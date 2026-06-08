package ch.elexis.core.services.holder;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IStoreToStringService;

public class StoreToStringServiceHolder {


	public static IStoreToStringService get() {
		return PortableServiceLoader.get(IStoreToStringService.class);
	}

	public static String getStoreToString(Object object) {
		if (object instanceof Identifiable) {
			return StoreToStringServiceHolder.get().storeToString((Identifiable) object)
					.orElseThrow(() -> new IllegalStateException("No storeToString for [" + object + "]"));
		}
		throw new IllegalStateException("No storeToString for [" + object + "]");
	}
}
