package ch.elexis.core.fhir.model.internal;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.myelexis.server.api.EntityManagementApi;

public class ManagementApiHolder {

	private static EntityManagementApi entityManagementApi;

	public static synchronized EntityManagementApi get() {
		if (entityManagementApi == null) {
			entityManagementApi = PortableServiceLoader.get(EntityManagementApi.class);
		}
		return entityManagementApi;
	}

}
