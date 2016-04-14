package ch.elexis.core.ui.locks;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListenerImpl;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;

public abstract class AbstractToggleCurrentLockHandler extends AbstractHandler
		implements IElementUpdater {
	
	protected ICommandService commandService;
	private ElexisEventListenerImpl eventListener;
	
	public abstract String getCommandId();
	
	public abstract Class<?> getTemplateClass();
	
	public AbstractToggleCurrentLockHandler(){
		eventListener = new ElexisUiEventListenerImpl(LockInfo.class,
			ElexisEvent.EVENT_RELOAD) {
			@Override
			public void runInUi(ElexisEvent ev){
				if (commandService == null) {
					commandService = (ICommandService) PlatformUI.getWorkbench()
						.getService(ICommandService.class);
				}
				commandService.refreshElements(getCommandId(), null);
			}
		};
		ElexisEventDispatcher.getInstance().addListeners(eventListener);
	}
	
	@Override
	protected void finalize() throws Throwable{
		ElexisEventDispatcher instance = ElexisEventDispatcher.getInstance();
		if (instance != null) {
			ElexisEventDispatcher.getInstance().removeListeners(eventListener);
		}
		super.finalize();
	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		if (commandService == null) {
			commandService = (ICommandService) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getService(ICommandService.class);
		}
		
		IPersistentObject po = ElexisEventDispatcher.getSelected(getTemplateClass());
		if (po == null) {
			commandService.refreshElements(getCommandId(), null);
			return null;
		}
		
		if (CoreHub.getLocalLockService().isLockedLocal(po)) {
			CoreHub.getLocalLockService().releaseLock(po);
		} else {
			LockResponse lr = CoreHub.getLocalLockService().acquireLock(po);
			if (!lr.isOk()) {
				LockResponseHelper.showInfo(lr, po, null);
			}
		}
		
		commandService.refreshElements(getCommandId(), null);
		
		return null;
	}
	
	@Override
	public void updateElement(UIElement element, Map parameters){
		IPersistentObject po = ElexisEventDispatcher.getSelected(getTemplateClass());
		if (po == null) {
			element.setIcon(Images.IMG_LOCK_CLOSED.getImageDescriptor());
			element.setChecked(false);
			return;
		}
		
		if (CoreHub.getLocalLockService().isLockedLocal(po)) {
			element.setIcon(Images.IMG_LOCK_OPEN.getImageDescriptor());
			element.setChecked(true);
		} else {
			element.setIcon(Images.IMG_LOCK_CLOSED.getImageDescriptor());
			element.setChecked(false);
		}
	}
	
}
