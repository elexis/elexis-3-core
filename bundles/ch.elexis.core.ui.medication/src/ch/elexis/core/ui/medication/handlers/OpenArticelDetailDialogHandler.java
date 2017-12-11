package ch.elexis.core.ui.medication.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.dialogs.ArtikelDetailDialog;
import ch.elexis.data.Prescription;

public class OpenArticelDetailDialogHandler extends AbstractHandler implements IHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		Prescription prescription = (Prescription) ElexisEventDispatcher.getSelected(Prescription.class);
		if(prescription != null) {
			ArtikelDetailDialog dd = new ArtikelDetailDialog(Display.getDefault().getActiveShell(),
				prescription.getArtikel());
			dd.open();
		}
		return null;
	}
}
