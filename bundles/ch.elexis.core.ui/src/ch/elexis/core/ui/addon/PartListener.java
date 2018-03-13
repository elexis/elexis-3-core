
package ch.elexis.core.ui.addon;

import javax.inject.Inject;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.osgi.service.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;

public class PartListener {
	
	private Logger log = LoggerFactory.getLogger(PartListener.class);
	
	@Inject
	PartListener(IEventBroker eventBroker){
		eventBroker.subscribe(ElexisEventTopics.BASE+"*", this::handleVisibleChildrenChanged);
	}
	
	void handleVisibleChildrenChanged(Event event){
		System.out.println(event);
//		MUIElement property = (MUIElement) event.getProperty(UIEvents.EventTags.ELEMENT);
//		String elementId = property.getElementId();
//		if(FaelleView.ID.equals(elementId)) {
//			log.debug(property.getElementId()+" "+event.getProperty(UIEvents.EventTags.OLD_VALUE)+"->"+event.getProperty(UIEvents.EventTags.NEW_VALUE));
//		}

	}
	
}
