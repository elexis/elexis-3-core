/*******************************************************************************
 * Copyright (c) 2017 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.commands;

import java.util.Collections;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.ui.dialogs.BillingProposalWizardDialog;
import ch.elexis.core.ui.views.rechnung.BillingProposalView;

/**
 * Handler opening a {@link BillingProposalWizardDialog} and setting the input of the
 * {@link BillingProposalView}.
 * 
 * @author thomas
 *
 */
public class StartBillingProposalHandler extends AbstractHandler implements IHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		try {
			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
			IWorkbenchPage page = window.getActivePage();
			BillingProposalView view = (BillingProposalView) page.showView(BillingProposalView.ID);
			
			BillingProposalWizardDialog dialog =
				new BillingProposalWizardDialog(HandlerUtil.getActiveShell(event));
			if (dialog.open() == Dialog.OK) {
				view.setInput(dialog.getProposal().orElse(Collections.emptyList()));
			}
		} catch (PartInitException e) {
			MessageDialog.openError(HandlerUtil.getActiveShell(event), "Fehler",
				"Konnte Rechnungs-Vorschlag View nicht öffnen");
		}
		
		return null;
	}
}
