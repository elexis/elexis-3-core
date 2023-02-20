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
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListenerImpl;
import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.data.service.StoreToStringServiceHolder;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IDocumentLetter;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IContext;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.dialogs.SelectFallNoObligationDialog;
import ch.elexis.core.ui.util.CoreUiUtil;
import ch.elexis.data.Anwender;
import ch.elexis.data.Brief;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Mandant;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Prescription;
import ch.elexis.data.User;

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

	private SelectionEventDispatcherListener eventDispatcherListener;

	private ReloadEventDispatcherListener reloadEventDispatcherListener;

	private LockingEventDispatcherListener lockingEventDispatcherListener;

	private UserChangedEventDispatcherListener userChangedEventDispatcherListener;

	private MandatorChangedEventDispatcherListener mandatorChangedEventDispatcherListener;

	private CompatibilityEventDispatcherListener compatibilityEventDispatcherListener;

	private IEclipseContext applicationContext;

	@Reference
	private EventAdmin eventAdmin;

	@Activate
	public void activate() {
		logger.info("ACTIVATE"); //$NON-NLS-1$
		root = new Context();
		contexts = new ConcurrentHashMap<>();
		eventDispatcherListener = new SelectionEventDispatcherListener();
		reloadEventDispatcherListener = new ReloadEventDispatcherListener();
		lockingEventDispatcherListener = new LockingEventDispatcherListener();
		userChangedEventDispatcherListener = new UserChangedEventDispatcherListener();
		mandatorChangedEventDispatcherListener = new MandatorChangedEventDispatcherListener();
		compatibilityEventDispatcherListener = new CompatibilityEventDispatcherListener();
		ElexisEventDispatcher elexisEventDispatcher = ElexisEventDispatcher.getInstance();
		LoggerFactory.getLogger(getClass()).info("Attaching to " + elexisEventDispatcher); //$NON-NLS-1$
		elexisEventDispatcher.addListeners(eventDispatcherListener, reloadEventDispatcherListener,
				lockingEventDispatcherListener, userChangedEventDispatcherListener,
				mandatorChangedEventDispatcherListener, compatibilityEventDispatcherListener);

		getRootContext().setNamed(IContext.STATION_IDENTIFIER, CoreHub.getStationIdentifier());

		registerCoreUiSuppliers();
		elexisEventDispatcher.registerFallbackConsumer(this);
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
		ElexisEventDispatcher.getInstance().removeListeners(eventDispatcherListener, reloadEventDispatcherListener,
				lockingEventDispatcherListener, userChangedEventDispatcherListener,
				mandatorChangedEventDispatcherListener, compatibilityEventDispatcherListener);
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
			CoreUiUtil.injectServices(ElexisEventDispatcher.getInstance(), applicationContext);
		}
		// set initial values for injection
		applicationContext.set(IUser.class, getRootContext().getTyped(IUser.class).orElse(null));
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

	private class LockingEventDispatcherListener extends ElexisEventListenerImpl {
		public LockingEventDispatcherListener() {
			super(null, null, ElexisEvent.EVENT_LOCK_AQUIRED | ElexisEvent.EVENT_LOCK_RELEASED
					| ElexisEvent.EVENT_LOCK_PRERELEASE, 0);
		}

		@Override
		public void catchElexisEvent(ElexisEvent ev) {
			Object object = ev.getGenericObject();
			if (object == null) {
				object = ev.getObject();
				if (object == null) {
					object = ev.getObjectClass();
				}
			}
			if (ev.getType() == ElexisEvent.EVENT_LOCK_AQUIRED) {
				postEvent(ElexisEventTopics.EVENT_LOCK_AQUIRED, getModelObjectForPersistentObject(object));
			} else if (ev.getType() == ElexisEvent.EVENT_LOCK_RELEASED) {
				postEvent(ElexisEventTopics.EVENT_LOCK_RELEASED, getModelObjectForPersistentObject(object));
			} else if (ev.getType() == ElexisEvent.EVENT_LOCK_PRERELEASE) {
				postEvent(ElexisEventTopics.EVENT_LOCK_PRERELEASE, getModelObjectForPersistentObject(object), true);
			}
		}
	}

	private class ReloadEventDispatcherListener extends ElexisEventListenerImpl {
		public ReloadEventDispatcherListener() {
			super(null, null, ElexisEvent.EVENT_RELOAD | ElexisEvent.EVENT_UPDATE, 0);
		}

		@Override
		public void catchElexisEvent(ElexisEvent ev) {
			Object object = ev.getGenericObject();
			if (object == null) {
				object = ev.getObject();
				if (object == null) {
					object = ev.getObjectClass();
				}
			}
			if (ev.getType() == ElexisEvent.EVENT_RELOAD) {
				if (object instanceof Class<?>) {
					Optional<Class<?>> modelClass = getCoreModelInterfaceForElexisClass((Class<?>) object);
					if (modelClass.isPresent()) {
						postEvent(ElexisEventTopics.EVENT_RELOAD, modelClass.get());
					} else {
						logger.debug("Could not get model class for [" + object + "] ignored reload event"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				} else {
					postEvent(ElexisEventTopics.EVENT_RELOAD, getModelObjectForPersistentObject(object));
				}
			} else if (ev.getType() == ElexisEvent.EVENT_UPDATE) {
				Object modelObject = getModelObjectForPersistentObject(object);
				// refresh object due to change performed by PersistentObject
				if (modelObject instanceof Identifiable) {
					CoreModelServiceHolder.get().refresh((Identifiable) modelObject, true);
				}
				postEvent(ElexisEventTopics.EVENT_UPDATE, modelObject);
			}
		}
	}

	private class CompatibilityEventDispatcherListener extends ElexisEventListenerImpl {
		public CompatibilityEventDispatcherListener() {
			super(null, null, ElexisEvent.EVENT_CREATE | ElexisEvent.EVENT_DELETE | ElexisEvent.EVENT_RELOAD, 0);
		}

		@Override
		public void catchElexisEvent(ElexisEvent ev) {
			Object object = ev.getGenericObject();
			if (object == null) {
				object = ev.getObject();
				if (object == null) {
					object = ev.getObjectClass();
				}
			}
			if (ev.getType() == ElexisEvent.EVENT_CREATE) {
				postEvent(ElexisEventTopics.PERSISTENCE_EVENT_COMPATIBILITY_CREATE,
						getModelObjectForPersistentObject(object));
			} else if (ev.getType() == ElexisEvent.EVENT_DELETE) {
				postEvent(ElexisEventTopics.PERSISTENCE_EVENT_COMPATIBILITY_DELETE,
						getModelObjectForPersistentObject(object));
			} else if (ev.getType() == ElexisEvent.EVENT_RELOAD) {
				if (object instanceof Class<?>) {
					postEvent(ElexisEventTopics.PERSISTENCE_EVENT_COMPATIBILITY_RELOAD,
							getCoreModelInterfaceForElexisClass((Class<?>) object).orElse(null));
				} else {
					postEvent(ElexisEventTopics.PERSISTENCE_EVENT_COMPATIBILITY_RELOAD,
							getModelObjectForPersistentObject(object));
				}
			}
		}
	}

	private class UserChangedEventDispatcherListener extends ElexisEventListenerImpl {
		public UserChangedEventDispatcherListener() {
			super(null, null, ElexisEvent.EVENT_USER_CHANGED, 0);
		}

		@Override
		public void catchElexisEvent(ElexisEvent ev) {
			Object object = ev.getGenericObject();
			if (object == null) {
				object = ev.getObject();
				if (object == null) {
					object = ev.getObjectClass();
				}
			}
			if (object instanceof User) {
				Optional<IUser> iUser = coreModelService.load(((User) object).getId(), IUser.class);
				iUser.ifPresent(u -> root.setTyped(u));
			}

			IUser user = root.getTyped(IUser.class).orElse(null);
			postEvent(ElexisEventTopics.EVENT_USER_CHANGED, user);
		}

	}

	private class MandatorChangedEventDispatcherListener extends ElexisEventListenerImpl {
		public MandatorChangedEventDispatcherListener() {
			super(null, null, ElexisEvent.EVENT_MANDATOR_CHANGED, 0);
		}

		@Override
		public void catchElexisEvent(ElexisEvent ev) {
			Object object = ev.getGenericObject();
			if (object == null) {
				object = ev.getObject();
				if (object == null) {
					object = ev.getObjectClass();
				}
			}
			if (object instanceof Mandant) {
				Optional<IMandator> iMandator = coreModelService.load(((Mandant) object).getId(), IMandator.class);
				iMandator.ifPresent(m -> root.setTyped(m));
			}
		}
	}

	/**
	 * If the object is instance of {@link PersistentObject} the
	 * {@link StoreToStringServiceHolder} is used to reload the object as a model
	 * object. If the object is not an instance of {@link PersistentObject} the same
	 * object is returned.
	 *
	 * @return
	 */
	private Object getModelObjectForPersistentObject(Object object) {
		if (object instanceof PersistentObject) {
			String storeToString = ((PersistentObject) object).storeToString();
			Optional<Identifiable> loaded = StoreToStringServiceHolder.get().loadFromString(storeToString);
			if (loaded.isPresent()) {
				return loaded.get();
			}
		}
		return object;
	}

	private class SelectionEventDispatcherListener extends ElexisEventListenerImpl {

		public SelectionEventDispatcherListener() {
			super(null, null, ElexisEvent.EVENT_SELECTED | ElexisEvent.EVENT_DESELECTED, 0);
		}

		@Override
		public void catchElexisEvent(ElexisEvent ev) {
			if (ev.getType() == ElexisEvent.EVENT_SELECTED) {
				addObjectToRoot(getElexisEventObject(ev));
			} else if (ev.getType() == ElexisEvent.EVENT_DESELECTED) {
				removeObjectFromRoot(getElexisEventObject(ev));
			}
		}

		private Object getElexisEventObject(ElexisEvent ev) {
			Object obj = ev.getObject();
			if (obj == null) {
				obj = ev.getGenericObject();
				if (obj == null) {
					obj = ev.getObjectClass();
				}
			}
			return obj;
		}

		private void removeObjectFromRoot(Object object) {
			if (object != null) {
				logger.info("[DESEL] " + object.getClass() + "  || " + toString(object));
			}
			if (object instanceof Class<?>) {
				root.removeTyped((Class<?>) object);
				getCoreModelInterfaceForElexisClass((Class<?>) object).ifPresent(c -> root.removeTyped(c));
			} else if (object != null) {
				root.removeTyped(object.getClass());

			}
		}

		private void addObjectToRoot(Object object) {
			if (object != null) {
				logger.info("[SEL] " + object.getClass() + "  || " + toString(object));
			}
			root.setTyped(getModelObjectForPersistentObject(object));
		}

		private String toString(Object obj) {
			if (obj == null) {
				return "null";
			}
			if (obj instanceof PersistentObject) {
				PersistentObject po = (PersistentObject) obj;
				return po.getId();
			}
			if (obj instanceof Identifiable) {
				Identifiable identifiable = (Identifiable) obj;
				return identifiable.getId();
			}
			return obj.toString();
		}
	}

	private Optional<Class<?>> getCoreModelInterfaceForElexisClass(Class<?> elexisClazz) {
		if (elexisClazz == User.class) {
			return Optional.of(IUser.class);
		} else if (elexisClazz == Anwender.class) {
			return Optional.of(IContact.class);
		} else if (elexisClazz == Mandant.class) {
			return Optional.of(IMandator.class);
		} else if (elexisClazz == Patient.class) {
			return Optional.of(IPatient.class);
		} else if (elexisClazz == Konsultation.class) {
			return Optional.of(IEncounter.class);
		} else if (elexisClazz == Fall.class) {
			return Optional.of(ICoverage.class);
		} else if (elexisClazz == Prescription.class) {
			return Optional.of(IPrescription.class);
		} else if (elexisClazz == Brief.class) {
			return Optional.of(IDocumentLetter.class);
		}
		return Optional.empty();
	}
}
