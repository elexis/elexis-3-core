package ch.elexis.core.ui.locks;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

import ch.elexis.core.ui.Hub;

public class LockStatusDialogHandler extends AbstractHandler implements IElementUpdater {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		LockStatusDialog lockStatusDialog = new LockStatusDialog(Hub.getActiveShell());
		lockStatusDialog.open();
		return null;
	}
	
	@Override
	public void updateElement(UIElement element, Map parameters){
		// TODO Auto-generated method stub
		
	}
	
}
