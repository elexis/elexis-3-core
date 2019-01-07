/*******************************************************************************
 * Copyright (c) 2018 IT-Med AG <info@it-med-ag.ch>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IT-Med AG <info@it-med-ag.ch> - initial implementation
 ******************************************************************************/

package ch.elexis.core.ui.commands;

import static ch.elexis.core.ui.text.TextTemplateRequirement.TT_PATIENT_LABEL;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.actions.Messages;
import ch.elexis.core.ui.dialogs.EtiketteDruckenDialog;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Patient;

public final class PrintPatientLabelHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Patient actPatient = (Patient) ElexisEventDispatcher.getSelected(Patient.class);
		if (actPatient == null) {
			SWTHelper.showInfo("Kein Patient ausgewählt", "Bitte wählen Sie vor dem Drucken einen Patient!");
			return null;
		}
		
		EtiketteDruckenDialog dlg = new EtiketteDruckenDialog(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), actPatient, TT_PATIENT_LABEL);
		dlg.setTitle(Messages.GlobalActions_PrintLabel);
		dlg.setMessage(Messages.GlobalActions_PrintLabelToolTip);
		if (!CoreHub.localCfg.get("Drucker/Etiketten/Choose", true)) {
			dlg.setBlockOnOpen(false);
			dlg.open();
			if (dlg.doPrint()) {
				dlg.close();
			} else {
				SWTHelper.alert("Fehler beim Drucken",
						"Beim Drucken ist ein Fehler aufgetreten. Bitte überprüfen Sie die Einstellungen.");
			}
		} else {
			dlg.setBlockOnOpen(true);
			dlg.open();
		}
		return null;
	}
}
