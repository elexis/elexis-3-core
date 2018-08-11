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
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.IContext;
import ch.elexis.core.services.IContextService;
import ch.elexis.data.Anwender;
import ch.elexis.data.Mandant;
import ch.elexis.data.Patient;
import ch.elexis.data.User;

@Component(property = EventConstants.EVENT_TOPIC + "=" + UIEvents.UILifeCycle.APP_STARTUP_COMPLETE)
public class ContextService implements IContextService, EventHandler {
	
	private Context root;
	
	private ConcurrentHashMap<String, Context> contexts;
	
	private SelectionEventDispatcherListener eventDispatcherListener;
	
	private ReloadEventDispatcherListener reloadEventDispatcherListener;
	
	private LockingEventDispatcherListener lockingEventDispatcherListener;
	
	private IEclipseContext applicationContext;
	
	@Reference
	private EventAdmin eventAdmin;
	
	@Activate
	public void activate(){
		root = new Context();
		eventDispatcherListener = new SelectionEventDispatcherListener();
		reloadEventDispatcherListener = new ReloadEventDispatcherListener();
		lockingEventDispatcherListener = new LockingEventDispatcherListener();
		ElexisEventDispatcher elexisEventDispatcher = ElexisEventDispatcher.getInstance();
		elexisEventDispatcher.addListeners(eventDispatcherListener, reloadEventDispatcherListener,
			lockingEventDispatcherListener);
		((Context) root).setElexisEventDispatcher(elexisEventDispatcher);
	}
	
	@Deactivate
	public void deactivate(){
		ElexisEventDispatcher.getInstance().removeListeners(eventDispatcherListener,
			reloadEventDispatcherListener, lockingEventDispatcherListener);
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
			super(null, null, ElexisEvent.EVENT_LOCK_AQUIRED | ElexisEvent.EVENT_LOCK_RELEASED, 0);
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
				postEvent(ElexisEventTopics.EVENT_LOCK_AQUIRED, object);
			} else if (ev.getType() == ElexisEvent.EVENT_LOCK_RELEASED) {
				postEvent(ElexisEventTopics.EVENT_LOCK_RELEASED, object);
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
				postEvent(ElexisEventTopics.EVENT_RELOAD, object);
			} else if (ev.getType() == ElexisEvent.EVENT_UPDATE) {
				postEvent(ElexisEventTopics.EVENT_UPDATE, object);
			}
		}
	}
	
	private class SelectionEventDispatcherListener extends ElexisEventListenerImpl {
		
		public SelectionEventDispatcherListener(){
			super(null, null, ElexisEvent.EVENT_SELECTED | ElexisEvent.EVENT_DESELECTED, 0);
		}
		
		@Override
		public void catchElexisEvent(ElexisEvent ev){
			if (ev.getType() == ElexisEvent.EVENT_SELECTED) {
				addObjectToRoot(ev.getObject());
			} else if (ev.getType() == ElexisEvent.EVENT_DESELECTED) {
				removeObjectFromRoot(ev.getObject());
			}
		}
		
		private void removeObjectFromRoot(Object object){
			if (object instanceof User) {
				root.setActiveUser(null);
			} else if (object instanceof Anwender) {
				root.setActiveUserContact(null);
			} else if (object instanceof Mandant) {
				root.setActiveMandator(null);
			} else if (object instanceof Patient) {
				root.setActivePatient(null);
			} else if (object != null) {
				root.setTyped(object);
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
			} else if (object instanceof Mandant) {
				Optional<IMandator> iMandator =
					CoreModelServiceHolder.get().load(((Mandant) object).getId(), IMandator.class);
				iMandator.ifPresent(m -> root.setActiveMandator(m));
			} else if (object instanceof Patient) {
				Optional<IPatient> iPatient =
					CoreModelServiceHolder.get().load(((Patient) object).getId(), IPatient.class);
				iPatient.ifPresent(p -> root.setActivePatient(p));
			} else if (object != null) {
				root.setTyped(object);
			}
		}
	}
}
