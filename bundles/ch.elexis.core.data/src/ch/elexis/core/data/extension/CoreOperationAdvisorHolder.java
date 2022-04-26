package ch.elexis.core.data.extension;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @since 3.8 replaced extension point based solution
 */
@Component(immediate = true)
public class CoreOperationAdvisorHolder {

	private static ICoreOperationAdvisor coreOperationAdvisor;

	@Reference
	public void setModelService(ICoreOperationAdvisor coreOperationAdvisor) {
		CoreOperationAdvisorHolder.coreOperationAdvisor = coreOperationAdvisor;
	}

	public static ICoreOperationAdvisor get() {
		if (coreOperationAdvisor == null) {
			throw new IllegalStateException("No coreOperationAdvisor available!");
		}
		return coreOperationAdvisor;
	}

}
