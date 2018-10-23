package ch.elexis.core.ui.services.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListenerImpl;
import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.data.service.StoreToStringServiceHolder;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IContext;
import ch.elexis.core.services.IContextService;
import ch.elexis.data.Anwender;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Mandant;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.User;

/**
 * This {@link IContextService} implementation translates events from and to the
 * {@link ElexisEventDispatcher} and the {@link IEclipseContext}.
 * 
 * <p>
 * <b>{@link ElexisEventDispatcher}</b><br/>
 * Selection, Reload and Locking Events are translated to {@link ElexisEventTopics} and posted using
 * the {@link EventAdmin}. If the event referrer to an object, the object is translated to an
 * {@link Identifiable} using the {@link StoreToStringServiceHolder}. Only events from the
 * {@link ElexisEventDispatcher} are consumed, no events are sent.
 * </p>
 * 
 * <p>
 * <b>{@link IEclipseContext}</b><br/>
 * On startup complete the context is initialized from the {@link MApplication} and passed to the
 * root {@link Context}. The {@link Context} will pass all set (named, typed, etc.) to the
 * applications {@link IEclipseContext}. This enables the e4 injection for changes.
 * </p>
 * 
 * @author thomas
 *
 */
@Component(property = EventConstants.EVENT_TOPIC + "=" + UIEvents.UILifeCycle.APP_STARTUP_COMPLETE)
public class ContextService implements IContextService, EventHandler {
	
	private Context root;
	
	private ConcurrentHashMap<String, Context> contexts;
	
	private SelectionEventDispatcherListener eventDispatcherListener;
	
	private ReloadEventDispatcherListener reloadEventDispatcherListener;
	
	private LockingEventDispatcherListener lockingEventDispatcherListener;
	
	private UserChangedEventDispatcherListener userChangedEventDispatcherListener;
	
	private MandatorChangedEventDispatcherListener mandatorChangedEventDispatcherListener;
	
	private IEclipseContext applicationContext;
	
	@Reference
	private EventAdmin eventAdmin;
	
	@Activate
	public void activate(){
		root = new Context();
		eventDispatcherListener = new SelectionEventDispatcherListener();
		reloadEventDispatcherListener = new ReloadEventDispatcherListener();
		lockingEventDispatcherListener = new LockingEventDispatcherListener();
		userChangedEventDispatcherListener = new UserChangedEventDispatcherListener();
		mandatorChangedEventDispatcherListener = new MandatorChangedEventDispatcherListener();
		ElexisEventDispatcher elexisEventDispatcher = ElexisEventDispatcher.getInstance();
		elexisEventDispatcher.addListeners(eventDispatcherListener, reloadEventDispatcherListener,
			lockingEventDispatcherListener, userChangedEventDispatcherListener,
			mandatorChangedEventDispatcherListener);
		((Context) root).setElexisEventDispatcher(elexisEventDispatcher);
	}
	
	@Deactivate
	public void deactivate(){
		ElexisEventDispatcher.getInstance().removeListeners(eventDispatcherListener,
			reloadEventDispatcherListener, lockingEventDispatcherListener,
			userChangedEventDispatcherListener, mandatorChangedEventDispatcherListener);
	}
	
	@Override
	public void handleEvent(Event event){
		Object property = event.getProperty("org.eclipse.e4.data");
		if (property instanceof MApplication) {
			MApplication application = (MApplication) property;
			applicationContext = application.getContext();
			if (getRootContext() != null) {
				((Context) getRootContext()).setEclipseContext(applicationContext);
			}
		}
	}
	
	@Override
	public IContext getRootContext(){
		return root;
	}
	
	@Override
	public Optional<IContext> getNamedContext(String name){
		return Optional.ofNullable(contexts.get(name));
	}
	
	@Override
	public IContext createNamedContext(String name){
		Context context = new Context(root, name);
		contexts.put(name, context);
		return context;
	}
	
	@Override
	public void releaseContext(String name){
		Context context = contexts.get(name);
		if (context != null) {
			context.setParent(null);
			contexts.remove(name);
		}
	}
	
	@Override
	public void postEvent(String topic, Object object){
		if (eventAdmin != null) {
			Map<String, Object> properites = new HashMap<>();
			properites.put("org.eclipse.e4.data", object);
			Event event = new Event(topic, properites);
			eventAdmin.postEvent(event);
		} else {
			throw new IllegalStateException("No EventAdmin available");
		}
	}
	
	private class LockingEventDispatcherListener extends ElexisEventListenerImpl {
		public LockingEventDispatcherListener(){
			super(null, null, ElexisEvent.EVENT_LOCK_AQUIRED | ElexisEvent.EVENT_LOCK_RELEASED
				| ElexisEvent.EVENT_LOCK_PRERELEASE, 0);
		}
		
		@Override
		public void catchElexisEvent(ElexisEvent ev){
			Object object = ev.getGenericObject();
			if (object == null) {
				object = ev.getObject();
				if (object == null) {
					object = ev.getObjectClass();
				}
			}
			if (ev.getType() == ElexisEvent.EVENT_LOCK_AQUIRED) {
				postEvent(ElexisEventTopics.EVENT_LOCK_AQUIRED,
					getModelObjectForPersistentObject(object));
			} else if (ev.getType() == ElexisEvent.EVENT_LOCK_RELEASED) {
				postEvent(ElexisEventTopics.EVENT_LOCK_RELEASED,
					getModelObjectForPersistentObject(object));
			} else if (ev.getType() == ElexisEvent.EVENT_LOCK_PRERELEASE) {
				postEvent(ElexisEventTopics.EVENT_LOCK_PRERELEASE,
					getModelObjectForPersistentObject(object));
			}
		}
	}
	
	private class ReloadEventDispatcherListener extends ElexisEventListenerImpl {
		public ReloadEventDispatcherListener(){
			super(null, null, ElexisEvent.EVENT_RELOAD | ElexisEvent.EVENT_UPDATE, 0);
		}
		
		@Override
		public void catchElexisEvent(ElexisEvent ev){
			Object object = ev.getGenericObject();
			if (object == null) {
				object = ev.getObject();
				if (object == null) {
					object = ev.getObjectClass();
				}
			}
			if (ev.getType() == ElexisEvent.EVENT_RELOAD) {
				postEvent(ElexisEventTopics.EVENT_RELOAD,
					getModelObjectForPersistentObject(object));
			} else if (ev.getType() == ElexisEvent.EVENT_UPDATE) {
				postEvent(ElexisEventTopics.EVENT_UPDATE,
					getModelObjectForPersistentObject(object));
			}
		}
	}
	
	private class UserChangedEventDispatcherListener extends ElexisEventListenerImpl {
		public UserChangedEventDispatcherListener(){
			super(null, null, ElexisEvent.EVENT_USER_CHANGED, 0);
		}
		
		@Override
		public void catchElexisEvent(ElexisEvent ev){
			Object object = ev.getGenericObject();
			if (object == null) {
				object = ev.getObject();
				if (object == null) {
					object = ev.getObjectClass();
				}
			}
			if (object instanceof User) {
				Optional<IUser> iUser =
					CoreModelServiceHolder.get().load(((User) object).getId(), IUser.class);
				iUser.ifPresent(u -> root.setActiveUser(u));
			}
			
			postEvent(ElexisEventTopics.EVENT_USER_CHANGED,
				getModelObjectForPersistentObject(object));
		}
	}
	
	private class MandatorChangedEventDispatcherListener extends ElexisEventListenerImpl {
		public MandatorChangedEventDispatcherListener(){
			super(null, null, ElexisEvent.EVENT_MANDATOR_CHANGED, 0);
		}
		
		@Override
		public void catchElexisEvent(ElexisEvent ev){
			Object object = ev.getGenericObject();
			if (object == null) {
				object = ev.getObject();
				if (object == null) {
					object = ev.getObjectClass();
				}
			}
			if (object instanceof Mandant) {
				Optional<IMandator> iMandator =
					CoreModelServiceHolder.get().load(((Mandant) object).getId(), IMandator.class);
				iMandator.ifPresent(m -> root.setActiveMandator(m));
			}
		}
	}
	
	/**
	 * If the object is instance of {@link PersistentObject} the {@link StoreToStringServiceHolder}
	 * is used to reload the object as a model object. If the object is not an instance of
	 * {@link PersistentObject} the same object is returned.
	 * 
	 * @return
	 */
	private Object getModelObjectForPersistentObject(Object object){
		if (object instanceof PersistentObject) {
			String storeToString = ((PersistentObject) object).storeToString();
			Optional<Identifiable> loaded =
				StoreToStringServiceHolder.get().loadFromString(storeToString);
			if (loaded.isPresent()) {
				return loaded.get();
			}
		}
		return object;
	}
	
	private class SelectionEventDispatcherListener extends ElexisEventListenerImpl {
		
		public SelectionEventDispatcherListener(){
			super(null, null, ElexisEvent.EVENT_SELECTED | ElexisEvent.EVENT_DESELECTED, 0);
		}
		
		@Override
		public void catchElexisEvent(ElexisEvent ev){
			if (ev.getType() == ElexisEvent.EVENT_SELECTED) {
				addObjectToRoot(getElexisEventObject(ev));
			} else if (ev.getType() == ElexisEvent.EVENT_DESELECTED) {
				removeObjectFromRoot(getElexisEventObject(ev));
			}
		}
		
		private Object getElexisEventObject(ElexisEvent ev){
			Object obj = ev.getObject();
			if (obj == null) {
				obj = ev.getGenericObject();
				if (obj == null) {
					obj = ev.getObjectClass();
				}
			}
			return obj;
		}
		
		private void removeObjectFromRoot(Object object){
			if (object instanceof User) {
				root.setActiveUser(null);
			} else if (object instanceof Anwender) {
				root.setActiveUserContact(null);
			} else if (object instanceof Patient) {
				root.setActivePatient(null);
			} else if (object instanceof Class<?>) {
				root.removeTyped((Class<?>) object);
				getCoreModelInterfaceForElexisClass((Class<?>) object)
					.ifPresent(c -> root.removeTyped(c));
			} else if (object != null) {
				root.removeTyped(object.getClass());
			}
		}
		
		private void addObjectToRoot(Object object){
			if (object instanceof User) {
				Optional<IUser> iUser =
					CoreModelServiceHolder.get().load(((User) object).getId(), IUser.class);
				iUser.ifPresent(u -> root.setActiveUser(u));
			} else if (object instanceof Anwender) {
				Optional<IContact> iUserContact =
					CoreModelServiceHolder.get().load(((Anwender) object).getId(), IContact.class);
				iUserContact.ifPresent(c -> root.setActiveUserContact(c));
			} else if (object instanceof Patient) {
				Optional<IPatient> iPatient =
					CoreModelServiceHolder.get().load(((Patient) object).getId(), IPatient.class);
				iPatient.ifPresent(p -> root.setActivePatient(p));
			} else if (object != null) {
				root.setTyped(getModelObjectForPersistentObject(object));
			}
		}
		
		private Optional<Class<?>> getCoreModelInterfaceForElexisClass(Class<?> elexisClazz){
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
			}
			return Optional.empty();
		}
	}
}
