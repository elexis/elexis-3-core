package ch.elexis.core.ui.services;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListenerImpl;
import ch.elexis.core.data.service.ModelServiceHolder;
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

@Component
public class ContextService implements IContextService {
	
	private Context root;
	
	private ConcurrentHashMap<String, Context> contexts;
	
	private EventDispatcherListener eventDispatcherListener;
	
	@Activate
	public void activate(){
		root = new Context();
		eventDispatcherListener = new EventDispatcherListener();
		ElexisEventDispatcher.getInstance().addListeners(eventDispatcherListener);
	}
	
	@Deactivate
	public void deactivate(){
		ElexisEventDispatcher.getInstance().removeListeners(eventDispatcherListener);
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
	
	private class EventDispatcherListener extends ElexisEventListenerImpl {
		
		public EventDispatcherListener(){
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
					ModelServiceHolder.get().load(((User) object).getId(), IUser.class);
				iUser.ifPresent(u -> root.setActiveUser(u));
			} else if (object instanceof Anwender) {
				Optional<IContact> iUserContact =
					ModelServiceHolder.get().load(((Anwender) object).getId(), IContact.class);
				iUserContact.ifPresent(c -> root.setActiveUserContact(c));
			} else if (object instanceof Mandant) {
				Optional<IMandator> iMandator =
					ModelServiceHolder.get().load(((Mandant) object).getId(), IMandator.class);
				iMandator.ifPresent(m -> root.setActiveMandator(m));
			} else if (object instanceof Patient) {
				Optional<IPatient> iPatient =
					ModelServiceHolder.get().load(((Patient) object).getId(), IPatient.class);
				iPatient.ifPresent(p -> root.setActivePatient(p));
			} else if (object != null) {
				root.setTyped(object);
			}
		}
	}
}
