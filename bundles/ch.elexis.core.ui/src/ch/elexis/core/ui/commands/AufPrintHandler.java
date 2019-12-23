package ch.elexis.core.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.ui.views.AUFZeugnis;
import ch.rgw.tools.ExHandler;

public class AufPrintHandler extends AbstractHandler implements IHandler {
	public static final String CMD_ID = "ch.elexis.core.ui.commands.AufPrint";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		try {
			IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().showView(AUFZeugnis.ID);
			if (viewPart instanceof AUFZeugnis) {
				((AUFZeugnis) viewPart).createAUZ();
			}
		} catch (Exception ex) {
			ExHandler.handle(ex);
		}
		return null;
	}
	
}
