package ch.elexis.core.model.test;

import ch.elexis.core.model.Identifiable;
import ch.elexis.core.rcp.utils.OsgiServiceUtil;
import ch.elexis.core.services.IStoreToStringService;

public class StoreToStringServiceHolder {

	private static IStoreToStringService storeToStringService;

	public static IStoreToStringService get() {
		if (storeToStringService == null) {
			storeToStringService = OsgiServiceUtil.getService(IStoreToStringService.class).get();
		}
		return storeToStringService;
	}

	public static String getStoreToString(Object object) {
		if (object instanceof Identifiable) {
			return StoreToStringServiceHolder.get().storeToString((Identifiable) object)
					.orElseThrow(() -> new IllegalStateException("No storeToString for [" + object + "]"));
		}
		throw new IllegalStateException("No storeToString for [" + object + "]");
	}
}