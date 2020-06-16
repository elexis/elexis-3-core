package ch.elexis.core.ui.stock.handler;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.icons.Images;

public class StockSCSToggleArticleOutlay extends AbstractHandler
		implements IHandler, IElementUpdater {
	
	@Override
	public Object execute(ExecutionEvent executionEvent) throws ExecutionException{
		
		Command command = executionEvent.getCommand();
		HandlerUtil.toggleCommandState(command);
		
		ICommandService commandService =
			(ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		commandService.refreshElements(executionEvent.getCommand().getId(), null);
		
		return null;
	}
	
	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters){
		boolean isSuspended =
			CoreHub.localCfg.get(Preferences.INVENTORY_MACHINE_SUSPEND_OUTLAY,
				Preferences.INVENTORY_MACHINE_SUSPEND_OUTLAY_DEFAULT);
		if (isSuspended) {
			element.setChecked(false);
			element.setIcon(Images.IMG_DRAWER.getImageDescriptor());
		} else {
			element.setChecked(true);
			element.setIcon(Images.IMG_DRAWER_ARROW.getImageDescriptor());
		}
	}
	
}
