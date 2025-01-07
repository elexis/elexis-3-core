package ch.elexis.core.data.extension;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @since 3.8 replaced extension point based solution
 */
@Component
public class CoreOperationAdvisorHolder {

	private static ICoreOperationAdvisor coreOperationAdvisor;

	@Reference
	public void setCoreOperationAdvisor(ICoreOperationAdvisor coreOperationAdvisor) {
		CoreOperationAdvisorHolder.coreOperationAdvisor = coreOperationAdvisor;
	}

	public static ICoreOperationAdvisor get() {
		return coreOperationAdvisor;
	}
}
