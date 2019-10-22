package ch.elexis.core.ui.chromium.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.ui.chromium.views.BrowserView;

public class OpenUrlHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		IWorkbenchPage activePage =
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		BrowserView view =
				(BrowserView) activePage.findView(BrowserView.ID);
		
		view.navigateTo();	
		return null;
	}
	

	
}
