package ch.elexis.core.ui.locks;

import java.util.Map;
import java.util.Objects;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.server.ILockService;
import ch.elexis.core.services.IElexisServerService.ConnectionStatus;
import ch.elexis.core.services.holder.ElexisServerServiceHolder;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.icons.Images;

@Component(property = org.osgi.service.event.EventConstants.EVENT_TOPIC + "="
	+ ElexisEventTopics.EVENT_RELOAD)
public class LockStatusDialogHandler extends AbstractHandler
		implements IElementUpdater, EventHandler {
	
	public static final String COMMAND_ID = "ch.elexis.core.ui.locks.LockStatusDialog";
	
	private ImageDescriptor localIcon;
	private ImageDescriptor remoteIcon;
	private ImageDescriptor standaloneIcon;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		LockStatusDialog lockStatusDialog = new LockStatusDialog(Hub.getActiveShell());
		lockStatusDialog.open();
		return null;
	}
	
	private void prepareIcons(){
		localIcon = Images.IMG_LOCK_CLOSED_YELLOW.getURLImageDescriptor();
		remoteIcon = Images.IMG_LOCK_CLOSED_GREEN.getURLImageDescriptor();
		standaloneIcon = Images.IMG_LOCK_CLOSED_GREY.getURLImageDescriptor();
	}
	
	@Override
	public void updateElement(UIElement element, Map parameters){
		if (localIcon == null || remoteIcon == null || standaloneIcon == null) {
			prepareIcons();
		}
		ConnectionStatus connectionStatus = ElexisServerServiceHolder.get().getConnectionStatus();
		
		if (connectionStatus == ConnectionStatus.STANDALONE) {
			element.setIcon(standaloneIcon);
		} else if (connectionStatus == ConnectionStatus.LOCAL) {
			element.setIcon(localIcon);
		} else if (connectionStatus == ConnectionStatus.REMOTE) {
			element.setIcon(remoteIcon);
		}
	}
	
	@Override
	public void handleEvent(Event event){
		Object property = event.getProperty(ElexisEventTopics.ECLIPSE_E4_DATA);
		if (Objects.equals(property, ILockService.class)) {
			ICommandService commandService =
				PlatformUI.getWorkbench().getService(ICommandService.class);
			commandService.refreshElements(COMMAND_ID, null);
		}
	}
}
