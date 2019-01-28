package ch.elexis.core.ui.medication.handlers;

import java.util.Optional;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.model.IPrescription;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.dialogs.ArtikelDetailDialog;

public class OpenArticelDetailDialogHandler extends AbstractHandler implements IHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		Optional<IPrescription> prescription =
			ContextServiceHolder.get().getTyped(IPrescription.class);
		prescription.ifPresent(p -> {
			ArtikelDetailDialog dd = new ArtikelDetailDialog(Display.getDefault().getActiveShell(),
				p.getArticle());
			dd.open();
		});
		return null;
	}
}
