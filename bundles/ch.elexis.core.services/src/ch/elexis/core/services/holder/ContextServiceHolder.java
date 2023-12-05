package ch.elexis.core.services.holder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.model.IMandator;
import ch.elexis.core.services.IContextService;

@Component
public class ContextServiceHolder {

	private static IContextService contextService;

	@Reference
	public void setContextService(IContextService contextService) {
		ContextServiceHolder.contextService = contextService;
	}

	public static IContextService get() {
		if (contextService == null) {
			throw new IllegalStateException("No IContextService available"); //$NON-NLS-1$
		}
		return contextService;
	}

	public static boolean isAvailable() {
		return contextService != null;
	}

	public static IMandator getActiveMandatorOrNull() {
		if (contextService != null) {
			return contextService.getActiveMandator().orElse(null);
		}
		return null;
	}

	public static IMandator getActiveMandatorOrThrow() {
		if (contextService != null) {
			return contextService.getActiveMandator()
					.orElseThrow(() -> new IllegalStateException("No active IMandator found")); //$NON-NLS-1$
		}
		throw new IllegalStateException("No IContextService available"); //$NON-NLS-1$
	}
}
