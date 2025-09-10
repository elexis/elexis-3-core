package ch.elexis.core.ui.services.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.services.IContext;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.dialogs.SelectFallNoObligationDialog;

/**
 * This {@link IContextService} implementation translates events from and
 * manages selection with the {@link IEclipseContext}.
 *
 * <p>
 * <b>{@link IEclipseContext}</b><br/>
 * On startup complete the context is initialized from the {@link MApplication}
 * and passed to the root {@link Context}. The {@link Context} will pass all set
 * (named, typed, etc.) to the applications {@link IEclipseContext}. This
 * enables the e4 injection for changes.
 * </p>
 *
 * @author thomas
 *
 */
@Component
public class ContextService implements IContextService {

	private static Logger logger = LoggerFactory.getLogger(ContextService.class);

	private Context root;

	private ConcurrentHashMap<String, Context> contexts;

	private IEclipseContext applicationContext;

	@Reference
	private EventAdmin eventAdmin;

	private Consumer<RunAndTrack> runAndTrackConsumer;

	private List<RunAndTrack> delayedRunAndTrack = new ArrayList<>();

	public void addDelayedRunAndTrack() {
		synchronized (delayedRunAndTrack) {
			delayedRunAndTrack.forEach(rt -> runAndTrackConsumer.accept(rt));
		}
	}
	
	@Activate
	public void activate() {
		logger.info("ACTIVATE"); //$NON-NLS-1$
		root = new Context();
		contexts = new ConcurrentHashMap<>();
		getRootContext().setNamed(IContext.STATION_IDENTIFIER, CoreHub.getStationIdentifier());

		registerCoreUiFunctions();
	}

	private void registerCoreUiFunctions() {
		getRootContext().setNamed("SelectFallNoObligationDialog", new Supplier<ICoverage>() { //$NON-NLS-1$
			private ICoverage ret;

			@Override
			public synchronized ICoverage get() {
				ret = null;
				Optional<?> coverage = ContextServiceHolder.get().getNamed("SelectFallNoObligationDialog.coverage"); //$NON-NLS-1$
				Optional<?> billable = ContextServiceHolder.get().getNamed("SelectFallNoObligationDialog.billable"); //$NON-NLS-1$
				if (coverage.isPresent() && billable.isPresent()) {
					Display.getDefault().syncExec(() -> {
						SelectFallNoObligationDialog dlg = new SelectFallNoObligationDialog((ICoverage) coverage.get(),
								(IBillable) billable.get());
						if (dlg.open() == Dialog.OK) {
							ret = dlg.getCoverage();
						}
					});
				} else {
					logger.warn("SelectFallNoObligationDialog missing context parameter [" + coverage + "] [" + billable //$NON-NLS-1$ //$NON-NLS-2$
							+ "]"); //$NON-NLS-1$
				}
				return ret;
			}
		});

		runAndTrackConsumer = new Consumer<>() {
			@Override
			public void accept(RunAndTrack runAndTrack) {
				synchronized (delayedRunAndTrack) {
					if (applicationContext != null) {
						applicationContext.runAndTrack(runAndTrack);
					} else {
						delayedRunAndTrack.add(runAndTrack);
					}
				}
			}
		};
		getRootContext().setNamed("AddRunAndTrackToE4Context", runAndTrackConsumer);
	}

	@Deactivate
	public void deactivate() {
		logger.info("DEACTIVATE"); //$NON-NLS-1$
	}

//	@Override
//	public void handleEvent(Event event) {
//		Object property = event.getProperty("org.eclipse.e4.data"); //$NON-NLS-1$
//		if (property instanceof MApplication) {
//			logger.info("APPLICATION STARTUP COMPLETE " + property); //$NON-NLS-1$
//			MApplication application = (MApplication) property;
//			applicationContext = application.getContext();
//			if (getRootContext() != null) {
//				logger.info("SET APPLICATION CONTEXT " + applicationContext); //$NON-NLS-1$
//				((Context) getRootContext()).setEclipseContext(applicationContext);
//			}
//			addDelayedRunAndTrack();
//		}
//	}

	@Override
	public IContext getRootContext() {
		return root;
	}

	@Override
	public Optional<IContext> getNamedContext(String name) {
		return Optional.ofNullable(contexts.get(name));
	}

	@Override
	public IContext createNamedContext(String name) {
		Context context = new Context(root, name);
		contexts.put(name, context);
		return context;
	}

	@Override
	public void releaseContext(String name) {
		Context context = contexts.get(name);
		if (context != null) {
			context.setParent(null);
			contexts.remove(name);
		}
	}

	private void postEvent(String topic, Object object, Map<String, Object> additionalProperties, boolean synchronous) {
		if (eventAdmin != null) {
			Map<String, Object> properites = new HashMap<>();
			properites.put("org.eclipse.e4.data", object); //$NON-NLS-1$
			if (additionalProperties != null) {
				properites.putAll(additionalProperties);
			}
			Event event = new Event(topic, properites);
			if (synchronous) {
				eventAdmin.sendEvent(event);
			} else {
				eventAdmin.postEvent(event);
			}
		} else {
			throw new IllegalStateException("No EventAdmin available"); //$NON-NLS-1$
		}
	}

	@Override
	public void postEvent(String topic, Object object, Map<String, Object> additionalProperties) {
		postEvent(topic, object, additionalProperties, false);
	}

	@Override
	public void sendEvent(String topic, Object object, Map<String, Object> additionalProperties) {
		postEvent(topic, object, additionalProperties, true);
	}

	@Override
	public <T> T submitContextInheriting(Callable<T> callable) {
		try {
			return ForkJoinPool.commonPool().submit(callable).get();
		} catch (InterruptedException | ExecutionException e) {
			LoggerFactory.getLogger(getClass()).error("", e);
			return null;
		}
	}

	public void setApplication(MApplication application) {
		applicationContext = application.getContext();
		if (getRootContext() != null) {
			logger.info("SET APPLICATION CONTEXT " + applicationContext); //$NON-NLS-1$
			((Context) getRootContext()).setEclipseContext(applicationContext);
		}
		addDelayedRunAndTrack();
	}
}
