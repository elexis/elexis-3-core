package ch.elexis.core.findings.templates.ui.handler;

import java.util.Collections;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.templates.model.FindingsTemplate;
import ch.elexis.core.findings.templates.ui.dlg.FindingsSelectionDialog;
import ch.elexis.core.findings.templates.ui.views.FindingsView;

public class FindingsCreateHandler extends AbstractHandler implements IHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		FindingsSelectionDialog findingsSelectionDialog =
			new FindingsSelectionDialog(Display.getDefault().getActiveShell(),
				FindingsView.findingsTemplateService.getFindingsTemplates(),
				Collections.emptyList(), false);
		if (findingsSelectionDialog.open() == MessageDialog.OK) {
			FindingsTemplate selection = findingsSelectionDialog.getSingleSelection(false);
			if (selection != null) {
				IFinding iFinding = FindingsView.findingsTemplateService
					.createFinding(ElexisEventDispatcher.getSelectedPatient(), selection);
				
				ElexisEventDispatcher.getInstance()
					.fire(new ElexisEvent(iFinding, IFinding.class, ElexisEvent.EVENT_CREATE,
						ElexisEvent.PRIORITY_NORMAL));
			}
		}
		return null;
	}
	
}