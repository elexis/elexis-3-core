package ch.elexis.core.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.dialogs.EditAUFDialog;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.Messages;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;

public class AufNewHandler extends AbstractHandler implements IHandler {
	public static final String CMD_ID = "ch.elexis.core.ui.commands.AufNew";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		Patient pat = (Patient) ElexisEventDispatcher.getSelected(Patient.class);
		if (pat == null) {
			SWTHelper.showError(Messages.AUF2_NoPatientSelected, //$NON-NLS-1$
				Messages.AUF2_PleaseDoSelectPatient); //$NON-NLS-1$
			return null;
		}
		Konsultation kons = (Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
		Fall fall = null;
		if (kons != null) {
			fall = kons.getFall();
			if (fall == null) {
				SWTHelper.showError(Messages.AUF2_noCaseSelected, Messages.AUF2_selectCase); //$NON-NLS-1$ //$NON-NLS-2$
				return null;
				
			}
			if (!fall.getPatient().equals(pat)) {
				kons = null;
			}
		}
		if (kons == null) {
			kons = pat.getLetzteKons(false);
			if (kons == null) {
				SWTHelper.showError(Messages.AUF2_noCaseSelected, Messages.AUF2_selectCase); //$NON-NLS-1$ //$NON-NLS-2$
				return null;
			}
			fall = kons.getFall();
		}
		EditAUFDialog dlg =
			new EditAUFDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), null,
				fall);
		if (dlg.open() == Dialog.OK) {
			return dlg.getAuf();
		}
		return null;
	}
	
}
