package ch.elexis.core.mail.ui.preference;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/** Keeps service references alive for the lifetime of one preference page. */
final class MailSettingsServices implements AutoCloseable {
	private final BundleContext context = FrameworkUtil.getBundle(MailSettingsServices.class).getBundleContext();
	private final List<ServiceReference<?>> references = new ArrayList<>();

	<T> T get(Class<T> type) {
		ServiceReference<T> reference = context.getServiceReference(type);
		if (reference == null) {
			throw new IllegalStateException("Mail settings service unavailable"); //$NON-NLS-1$
		}
		T service = context.getService(reference);
		if (service == null) {
			throw new IllegalStateException("Mail settings service unavailable"); //$NON-NLS-1$
		}
		references.add(reference);
		return service;
	}

	/** Releases acquired references. */
	@Override
	public void close() {
		references.forEach(context::ungetService);
		references.clear();
	}
}
