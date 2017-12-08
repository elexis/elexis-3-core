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

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.ui.views.rechnung.BillingProposalView;
import ch.elexis.core.ui.views.rechnung.BillingProposalView.BillingInformation;

public class BillingProposalRemoveHandler extends AbstractHandler implements IHandler {
	
	@SuppressWarnings("unchecked")
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection != null && !selection.isEmpty()) {
			List<Object> selectionList = ((IStructuredSelection) selection).toList();
			// map to List<Konsultation> depending on type
			if (selectionList.get(0) instanceof BillingProposalView.BillingInformation) {
				BillingProposalView view = getOpenView(event);
				if (view != null) {
					view.removeToBill((List<BillingInformation>) (List<?>) selectionList);
				}
			}
		}
		return null;
	}
	
	private BillingProposalView getOpenView(ExecutionEvent event){
		try {
			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
			IWorkbenchPage page = window.getActivePage();
			return (BillingProposalView) page.showView(BillingProposalView.ID);
		} catch (PartInitException e) {
			MessageDialog.openError(HandlerUtil.getActiveShell(event), "Fehler",
				"Konnte Rechnungs-Vorschlag View nicht öffnen");
		}
		return null;
	}
}
