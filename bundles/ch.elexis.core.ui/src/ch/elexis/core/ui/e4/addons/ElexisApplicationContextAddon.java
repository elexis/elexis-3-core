package ch.elexis.core.ui.e4.addons;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;

/**
 * This Addon listens to incoming {@link ElexisEventTopics#CONTEXT_EVENT_SELECTION} messages, and
 * sets the respective {@link PersistentObject}s to the application context.
 */
public class ElexisApplicationContextAddon implements EventHandler {
	
	@Inject
	IEclipseContext context;
	
	@PostConstruct
	public void init(IEventBroker broker){
		broker.subscribe(ElexisEventTopics.CONTEXT_EVENT_SELECTION + "/*", this);
	}
	
	@Override
	public void handleEvent(Event event){
		Class<?> clazz = (Class<?>) event.getProperty(ElexisEventTopics.PROPKEY_CLASS);
		Object object = event.getProperty(ElexisEventTopics.PROPKEY_OBJECT);
		if (Patient.class.equals(clazz)) {
			context.set(Patient.class, (Patient) object);
		} else if (Fall.class.equals(clazz)) {
			context.set(Fall.class, (Fall) object);
		} else if (Konsultation.class.equals(clazz)) {
			context.set(Konsultation.class, (Konsultation) object);
		}
		System.out.println("WARN----"+event);
	}
}
