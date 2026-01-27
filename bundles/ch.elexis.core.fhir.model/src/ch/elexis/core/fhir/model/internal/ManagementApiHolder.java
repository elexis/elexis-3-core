package ch.elexis.core.fhir.model.internal;

import ch.elexis.core.utils.OsgiServiceUtil;
import ch.myelexis.server.api.EntityManagementApi;

public class ManagementApiHolder {

	private static EntityManagementApi entityManagementApi;

	public static synchronized EntityManagementApi get() {
		if (entityManagementApi == null) {
			entityManagementApi = OsgiServiceUtil.getServiceWait(EntityManagementApi.class, 5000).get();
		}
		return entityManagementApi;
	}

}
