package ch.elexis.core.internal;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import ch.elexis.core.logging.Slf4jLogListener;

public class Activator implements BundleActivator {

	private ILogListener logListener = new Slf4jLogListener();

	@Override
	public void start(BundleContext context) throws Exception {
		Platform.addLogListener(logListener);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		Platform.removeLogListener(logListener);
	}

}
