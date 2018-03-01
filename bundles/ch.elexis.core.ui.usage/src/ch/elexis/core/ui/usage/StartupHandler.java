package ch.elexis.core.ui.usage;

import java.io.IOException;
import java.util.Date;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.usage.settings.UsageSettings;
import ch.elexis.core.ui.usage.util.StatisticsManager;
import ch.elexis.core.ui.util.PerspectiveUtil;

@Component(property = EventConstants.EVENT_TOPIC + "=" + UIEvents.UILifeCycle.APP_STARTUP_COMPLETE)
public class StartupHandler implements EventHandler {
	
	@Override
	public void handleEvent(Event event){
		if (CoreHub.globalCfg.get(UsageSettings.CONFIG_USAGE_STATISTICS, false)) {
			registerNotifications();
			StatisticsManager.getInstance().getStatistics().setFrom(new Date());
			// add start perspective to statistics
			MPerspective mPerspective = PerspectiveUtil.getActivePerspective();
			if (mPerspective != null) {
				StatisticsManager.getInstance().addCallingStatistic(mPerspective.getElementId(),
					true);
			}
		}
	}
	
	/**
	 * Register event listeners for various statistics. Including shutdown listener, exporting
	 * statistics to writable user directory statistics.
	 */
	private void registerNotifications(){
		//Command.DEBUG_COMMAND_EXECUTION = true; can be activated if logging for command execution is needed
		IEventBroker b = PlatformUI.getWorkbench().getService(IEventBroker.class);
		b.subscribe(UIEvents.UILifeCycle.BRINGTOTOP, new EventHandler() {
			@Override
			public void handleEvent(Event event){
				
				Object part = event.getProperty(UIEvents.EventTags.ELEMENT);
				if (part instanceof MPart) {
					MPart mPart = (MPart) part;
					
					if (mPart.isToBeRendered()) {
						StatisticsManager.getInstance().addCallingStatistic(mPart.getElementId(),
							false);
					}
					
				}
			}
		});
		
		b.subscribe("org/eclipse/e4/ui/model/ui/UIElement/toBeRendered/SET", new EventHandler() {
			@Override
			public void handleEvent(Event event){
				Object placeholder = event.getProperty(UIEvents.EventTags.ELEMENT);
				if (placeholder instanceof MPlaceholder) {
					MPlaceholder mPlaceholder = (MPlaceholder) placeholder;
					if (!mPlaceholder.isToBeRendered()) {
						StatisticsManager.getInstance()
							.addClosingStatistic(mPlaceholder.getElementId(), false);
					}
					
				}
			}
		});
		
		b.subscribe(UIEvents.UILifeCycle.APP_SHUTDOWN_STARTED, new EventHandler() {
			@Override
			public void handleEvent(Event event){
				
				try {
					StatisticsManager.getInstance().autoExportStatistics();
				} catch (IOException e) {
					LoggerFactory.getLogger(StartupHandler.class)
						.error("cannot export usage on application exist statistics", e);
				}
			}
		});
		
		b.subscribe("org/eclipse/e4/ui/model/ui/ElementContainer/selectedElement/SET",
			new EventHandler() {
				@Override
				public void handleEvent(Event event){
					Object stack = event.getProperty(UIEvents.EventTags.ELEMENT);
					if (stack instanceof MPerspectiveStack) {
						MPerspectiveStack mpartStack = (MPerspectiveStack) stack;
						MPerspective selectedPerspective = mpartStack.getSelectedElement();
						if (selectedPerspective != null) {
							StatisticsManager.getInstance()
								.addCallingStatistic(selectedPerspective.getElementId(), true);
						}
						
					}
				}
			});
	}
}
