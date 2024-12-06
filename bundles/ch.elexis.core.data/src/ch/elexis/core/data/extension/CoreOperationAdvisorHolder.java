package ch.elexis.core.data.extension;

import ch.elexis.core.utils.OsgiServiceUtil;

/**
 * @since 3.8 replaced extension point based solution
 */
public class CoreOperationAdvisorHolder {

	private static ICoreOperationAdvisor coreOperationAdvisor;

	public static ICoreOperationAdvisor get() {
		if (coreOperationAdvisor == null) {
			coreOperationAdvisor = OsgiServiceUtil.getService(ICoreOperationAdvisor.class).orElseThrow();
		}
		return coreOperationAdvisor;
	}

}
