package ch.elexis.core.ui.commands;

import java.util.Optional;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.EncounterServiceHolder;
import ch.elexis.core.ui.dialogs.EditAUFDialog;
import ch.elexis.core.ui.util.SWTHelper;

public class AufNewHandler extends AbstractHandler implements IHandler {
	public static final String CMD_ID = "ch.elexis.core.ui.commands.AufNew"; //$NON-NLS-1$

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Optional<IPatient> pat = ContextServiceHolder.get().getActivePatient();
		if (!pat.isPresent()) {
			SWTHelper.showError(Messages.Core_No_patient_selected, // $NON-NLS-1$
					Messages.AUF2_PleaseDoSelectPatient); // $NON-NLS-1$
			return null;
		}
		ICoverage fall = ContextServiceHolder.get().getRootContext().getTyped(ICoverage.class).orElse(null);
		if (fall == null) {
			Optional<IEncounter> kons = ContextServiceHolder.get().getRootContext().getTyped(IEncounter.class);
			if (kons.isPresent()) {
				fall = kons.get().getCoverage();
				if (!fall.getPatient().equals(pat.get())) {
					kons = null;
				}
			}
			if (kons == null) {
				kons = EncounterServiceHolder.get().getLatestEncounter(pat.get());
				fall = kons.get().getCoverage();
			}
		}
		if (fall == null) {
			SWTHelper.showError(Messages.Core_No_case_selected, Messages.AUF2_selectCase); // $NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
		EditAUFDialog dlg = new EditAUFDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), null,
				fall);
		if (dlg.open() == Dialog.OK) {
			return dlg.getAuf();
		}
		return null;
	}

}
