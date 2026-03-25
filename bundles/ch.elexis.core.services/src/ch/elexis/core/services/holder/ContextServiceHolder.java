package ch.elexis.core.services.holder;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.services.IContextService;

public class ContextServiceHolder {

	public static IContextService get() {
		return PortableServiceLoader.get(IContextService.class);
	}

	public static boolean isAvailable() {
		return PortableServiceLoader.get(IContextService.class) != null;
	}

	public static IMandator getActiveMandatorOrNull() {
		if (ContextServiceHolder.get() != null) {
			return ContextServiceHolder.get().getActiveMandator().orElse(null);
		}
		return null;
	}

	public static IMandator getActiveMandatorOrThrow() {
		if (ContextServiceHolder.get() != null) {
			return ContextServiceHolder.get().getActiveMandator()
					.orElseThrow(() -> new IllegalStateException("No active IMandator found")); //$NON-NLS-1$
		}
		throw new IllegalStateException("No IContextService available"); //$NON-NLS-1$
	}
}
