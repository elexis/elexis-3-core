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

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.util.BillingUtil;
import ch.elexis.core.ui.views.rechnung.BillingProposalView;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Rechnung;
import ch.elexis.data.Rechnungssteller;
import ch.rgw.tools.Result;
import ch.rgw.tools.TimeTool;

public class BillingProposalViewCreateBillsHandler extends AbstractHandler implements IHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		List<Konsultation> kons = getToBill(event);
		final List<Konsultation> toBill =
			BillingUtil.getKonsultationsFromSameYear(kons);
		if (toBill.size() > 0 && toBill.size() != kons.size()) {
			if (!MessageDialog.openQuestion(HandlerUtil.getActiveShell(event),
				"Rechnung Validierung",
				"Eine Rechnung kann nur Leistungen innerhalb eines Jahres beinhalten.\n\nWollen Sie mit der Erstellung der Rechnung für das Jahr "
					+ new TimeTool(toBill.get(0).getDatum()).get(TimeTool.YEAR)
					+ " fortsetzen ?")) {
				LoggerFactory.getLogger(BillingProposalViewCreateBillsHandler.class)
					.warn("Invoice creation canceled by user");
				return null;
			}
		}
		
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(HandlerUtil.getActiveShell(event));
		try {
			dialog.run(true, false, new IRunnableWithProgress() {
				
				private int successful = 0;
				private int errorneous = 0;
				private StringBuilder errorneousInfo = new StringBuilder();
				
				@Override
				public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException{
					monitor.beginTask("Rechnungen erstellen", 3);
					List<Konsultation> billable = BillingUtil.filterNotBillable(toBill);
					monitor.worked(1);
					Map<Rechnungssteller, Map<Fall, List<Konsultation>>> toBillMap =
						BillingUtil.getGroupedBillable(billable);
					monitor.worked(1);
					// create all bills
					List<Result<Rechnung>> results = BillingUtil.createBills(toBillMap);
					// build information to show
					for (Result<Rechnung> result : results) {
						if (result.isOK()) {
							successful++;
						} else {
							errorneousInfo.append(result.getSeverity()).append(" -> ");
							List<Result<Rechnung>.msg> messages = result.getMessages();
							for (int i = 0; i < messages.size(); i++) {
								if (i > 0) {
									errorneousInfo.append(" / ");
								}
								errorneousInfo.append(messages.get(i).getText());
							}
							errorneousInfo.append("\n");
							errorneous++;
						}
					}
					monitor.worked(1);
					monitor.done();
					// show information
					Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run(){
							MessageDialog.openInformation(HandlerUtil.getActiveShell(event), "Info",
								MessageFormat.format(
									"Es wurden {0} Rechnungen erfolgreich erstellt.\nBei {1} Rechnungen traten Fehler auf.\n{2}",
									successful, errorneous, errorneousInfo.toString()));
						}
					});
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			MessageDialog.openError(HandlerUtil.getActiveShell(event), "Fehler",
				"Fehler beim Ausführen der Rechnungserstelltung. Details siehe Log.");
			LoggerFactory.getLogger(BillingProposalViewCreateBillsHandler.class)
				.error("Error creating bills", e);
			return null;
		}
		
		return null;
	}
	
	private List<Konsultation> getToBill(ExecutionEvent event){
		String selectionParameter =
			event.getParameter("ch.elexis.core.ui.BillingProposalViewCreateBills.selection");
		if ("selection".equals(selectionParameter)) {
			ISelection selection = HandlerUtil.getCurrentSelection(event);
			if (selection != null && !selection.isEmpty()) {
				@SuppressWarnings("unchecked")
				List<Object> selectionList = ((IStructuredSelection) selection).toList();
				// map to List<Konsultation> depending on type
				if (selectionList.get(0) instanceof Konsultation) {
					return selectionList.stream().map(o -> ((Konsultation) o))
						.collect(Collectors.toList());
				} else if (selectionList.get(0) instanceof BillingProposalView.BillingInformation) {
					return selectionList.stream()
						.map(o -> ((BillingProposalView.BillingInformation) o).getKonsultation())
						.collect(Collectors.toList());
				}
			}
		} else {
			BillingProposalView view = getOpenView(event);
			if (view != null) {
				return view.getToBill();
			}
		}
		return Collections.emptyList();
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
