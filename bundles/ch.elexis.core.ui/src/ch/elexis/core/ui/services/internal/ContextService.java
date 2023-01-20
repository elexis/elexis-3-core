package ch.elexis.core.ui.services.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.service.StoreToStringServiceHolder;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IContext;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.dialogs.SelectFallNoObligationDialog;

/**
 * This {@link IContextService} implementation translates events from and to the
 * {@link ElexisEventDispatcher} and the {@link IEclipseContext}.
 *
 * <p>
 * <b>{@link ElexisEventDispatcher}</b><br/>
 * Selection, Reload and Locking Events are translated to
 * {@link ElexisEventTopics} and posted using the {@link EventAdmin}. If the
 * event referrer to an object, the object is translated to an
 * {@link Identifiable} using the {@link StoreToStringServiceHolder}. Only
 * events from the {@link ElexisEventDispatcher} are consumed, no events are
 * sent.
 * </p>
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
@Component(property = EventConstants.EVENT_TOPIC + "=" + UIEvents.UILifeCycle.APP_STARTUP_COMPLETE)
public class ContextService implements IContextService, EventHandler {

	private static Logger logger = LoggerFactory.getLogger(ContextService.class);

	// do not use holder, if not direct dep. service is started too early
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;

	private Context root;

	private ConcurrentHashMap<String, Context> contexts;

	private IEclipseContext applicationContext;

	@Reference
	private EventAdmin eventAdmin;

	@Activate
	public void activate() {
		logger.info("ACTIVATE"); //$NON-NLS-1$
		root = new Context(this);
		contexts = new ConcurrentHashMap<>();
		getRootContext().setNamed(IContext.STATION_IDENTIFIER, CoreHub.getStationIdentifier());

		registerCoreUiSuppliers();
	}

	private void registerCoreUiSuppliers() {
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

	}

	@Deactivate
	public void deactivate() {
		logger.info("DEACTIVATE"); //$NON-NLS-1$
	}

	@Override
	public void handleEvent(Event event) {
		Object property = event.getProperty("org.eclipse.e4.data"); //$NON-NLS-1$
		if (property instanceof MApplication) {
			logger.info("APPLICATION STARTUP COMPLETE " + property); //$NON-NLS-1$
			MApplication application = (MApplication) property;
			applicationContext = application.getContext();
			if (getRootContext() != null) {
				logger.info("SET APPLICATION CONTEXT " + applicationContext); //$NON-NLS-1$
				((Context) getRootContext()).setEclipseContext(applicationContext);
			}
		}
	}

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
		Context context = new Context(root, name, this);
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

	private void postEvent(String topic, Object object, boolean synchronous) {
		if (eventAdmin != null) {
			Map<String, Object> properites = new HashMap<>();
			properites.put("org.eclipse.e4.data", object); //$NON-NLS-1$
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
	public void postEvent(String topic, Object object) {
		postEvent(topic, object, false);
	}

	@Override
	public void sendEvent(String topic, Object object) {
		postEvent(topic, object, true);
	}
}
