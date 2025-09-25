package ch.elexis.core.services.holder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.model.IMandator;
import ch.elexis.core.services.IContextService;

@Component
public class ContextServiceHolder {

	private static IContextService contextService;
	private static List<Runnable> waitForActiveUser;
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

	/**
	 * Führt das Runnable aus, sobald ein aktiver User im Context vorhanden ist.
	 */
	public synchronized static void runIfActiveUserAvailable(Runnable runnable) {
		if (contextService == null || contextService.getActiveUserContact().isEmpty()) {
			if (waitForActiveUser == null) {
				waitForActiveUser = new ArrayList<>();
				CompletableFuture.runAsync(() -> {
					while (contextService == null || contextService.getActiveUserContact().isEmpty()) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					}
					waitForActiveUser.forEach(r -> r.run());
				});
			}
			waitForActiveUser.add(runnable);
		} else {
			runnable.run();
		}
	}
}
