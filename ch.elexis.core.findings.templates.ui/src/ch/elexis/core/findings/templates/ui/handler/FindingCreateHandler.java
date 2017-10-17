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
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.templates.model.FindingsTemplate;
import ch.elexis.core.findings.templates.ui.dlg.FindingsSelectionDialog;
import ch.elexis.core.findings.templates.ui.util.FindingsServiceHolder;
import ch.elexis.core.findings.ui.util.FindingsUiUtil;
import ch.elexis.core.ui.UiDesk;

public class FindingCreateHandler extends AbstractHandler implements IHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		FindingsSelectionDialog findingsSelectionDialog =
			new FindingsSelectionDialog(
				Display.getDefault().getActiveShell(), FindingsServiceHolder.findingsTemplateService
					.getFindingsTemplates("Standard Vorlagen"),
				Collections.emptyList(), false, null, false);
		if (findingsSelectionDialog.open() == MessageDialog.OK) {
			FindingsTemplate selection = findingsSelectionDialog.getSingleSelection();
			if (selection != null) {
				IFinding iFinding;
				try {
					iFinding = FindingsServiceHolder.findingsTemplateService
						.createFinding(ElexisEventDispatcher.getSelectedPatient(), selection);
					
					Boolean okPressed = (Boolean) FindingsUiUtil
						.executeCommand("ch.elexis.core.findings.ui.commandEdit", iFinding);
					if (okPressed) {
						ElexisEventDispatcher.getInstance().fire(new ElexisEvent(iFinding,
							IFinding.class, ElexisEvent.EVENT_CREATE, ElexisEvent.PRIORITY_NORMAL));
						
						return iFinding;
					} else {
						// if cancel delete the created finding
						try {
							FindingsUiUtil.deleteObservation(iFinding);
						} catch (ElexisException e) {
							MessageDialog.openError(UiDesk.getDisplay().getActiveShell(), "Fehler",
								e.getMessage());
						}
					}
				} catch (ElexisException e) {
					MessageDialog.openWarning(Display.getDefault().getActiveShell(),
						"Befunde Vorlagen", e.getMessage());
				}
			}
		}
		return null;
	}
}