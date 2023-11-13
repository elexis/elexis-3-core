package ch.elexis.core.ui.commands;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ISickCertificate;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.dialogs.GenericPrintDialog;
import ch.elexis.core.ui.views.AUF2;
import ch.elexis.core.ui.views.AUF2.AUFContentProvider;

public class AufPrintListHandler extends AbstractHandler {
	public static final String CMD_ID = "ch.elexis.core.ui.commands.AufPrintList";
	private static final String PLACEHOLDER = "[Liste]";
	private static final String EMPTY_STRING = " ";
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell activeShell = HandlerUtil.getActiveShellChecked(event);
		AUF2 view = (AUF2) HandlerUtil.getActivePartChecked(event);
		IPatient pat = ContextServiceHolder.get().getActivePatient().orElse(null);
		if (pat != null) {
			Object[] elements = ((AUFContentProvider) view.getViewer().getContentProvider())
					.getElements(view.getViewer().getInput());
			List<Object> filteredElements = new ArrayList<>();
			for (Object element : elements) {
				if (!view.isFilterActive() || view.getFilter().select(view.getViewer(), null, element)) {
					filteredElements.add(element);
				}
			}
			if (!filteredElements.isEmpty()) {
				String[][] aufData = new String[filteredElements.size() + 2][];
				aufData[0] = new String[] { Messages.PATIENT_AUF + EMPTY_STRING + pat.getLabel() };
				aufData[1] = new String[] { EMPTY_STRING };
				for (int i = 0; i < filteredElements.size(); i++) {
					if (filteredElements.get(i) instanceof ISickCertificate) {
						ISickCertificate auf = (ISickCertificate) filteredElements.get(i);
						aufData[i + 2] = new String[] { auf.getLabel().toString() };
					}
				}
				GenericPrintDialog gpl = new GenericPrintDialog(activeShell, Messages.AUF_LISTE, Messages.AUF_LISTE);
				gpl.create();
				gpl.insertTable(PLACEHOLDER, aufData, null);
				gpl.open();
			} else {
				MessageDialog.openInformation(activeShell, Messages.INFORMATION,
						Messages.AUF_No_List);
			}
		} else {
			MessageDialog.openInformation(activeShell, Messages.INFORMATION,
					Messages.ChecklistView_KeinPatient);
		}
		return null;
	}
}
