package ch.elexis.core.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.model.ISickCertificate;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.views.AUFZeugnis;
import ch.rgw.tools.ExHandler;

public class AufPrintHandler extends AbstractHandler implements IHandler {
	public static final String CMD_ID = "ch.elexis.core.ui.commands.AufPrint"; //$NON-NLS-1$

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView(AUFZeugnis.ID);
			if (viewPart instanceof AUFZeugnis) {
				ISickCertificate selectedCertificate = ContextServiceHolder.get().getTyped(ISickCertificate.class)
						.orElse(null);
				if (selectedCertificate != null) {
					ContextServiceHolder.get().setTyped(selectedCertificate);
					((AUFZeugnis) viewPart).createAUZ();
				} else {
					MessageDialog.openInformation(Display.getDefault().getActiveShell(), "AUF drucken",
							"Es ist keine AUF selektiert.");
				}
			}
		} catch (Exception ex) {
			ExHandler.handle(ex);
		}
		return null;
	}

}
