package ch.elexis.core.ui.locks;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.lock.ILocalLockService;
import ch.elexis.core.lock.ILocalLockService.Status;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.icons.Images;

public class LockStatusDialogHandler extends AbstractHandler implements IElementUpdater {
	
	public static final String COMMAND_ID = "ch.elexis.core.ui.locks.LockStatusDialog";
	
	private ImageDescriptor localIcon;
	private ImageDescriptor remoteIcon;
	private ImageDescriptor standaloneIcon;
	
	public LockStatusDialogHandler(){
		ElexisEventDispatcher.getInstance().addListeners(
			new ElexisUiEventListenerImpl(ILocalLockService.class, ElexisEvent.EVENT_RELOAD) {
				private ICommandService commandService;
				
				@Override
				public void runInUi(ElexisEvent ev){
					if (commandService == null) {
						commandService = (ICommandService) PlatformUI.getWorkbench()
							.getService(ICommandService.class);
					}
					
					commandService.refreshElements(COMMAND_ID, null);
				}
			});
	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		LockStatusDialog lockStatusDialog = new LockStatusDialog(Hub.getActiveShell());
		lockStatusDialog.open();
		return null;
	}
	
	private void prepareIcons(){
		ImageDescriptor lockIcon = Images.IMG_LOCK_CLOSED.getImageDescriptor();
		localIcon = new DecorationOverlayIcon(lockIcon.createImage(), new ImageDescriptor[] {
			Images.IMG_BULLET_YELLOW.getImageDescriptor()
		});
		remoteIcon = new DecorationOverlayIcon(lockIcon.createImage(), new ImageDescriptor[] {
			Images.IMG_BULLET_GREEN.getImageDescriptor()
		});
		standaloneIcon = new DecorationOverlayIcon(lockIcon.createImage(), new ImageDescriptor[] {
			Images.IMG_BULLET_GREY.getImageDescriptor()
		});
	}
	
	@Override
	public void updateElement(UIElement element, Map parameters){
		if (localIcon == null || remoteIcon == null) {
			prepareIcons();
		}
		ILocalLockService.Status status = CoreHub.getLocalLockService().getStatus();
		
		if (status == Status.STANDALONE) {
			element.setIcon(standaloneIcon);
		} else if (status == Status.LOCAL) {
			element.setIcon(localIcon);
		} else if (status == Status.REMOTE) {
			element.setIcon(remoteIcon);
		}
	}
}
