/*******************************************************************************
 * Copyright (c) 2008-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *
 *******************************************************************************/
package ch.elexis.core.ui.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.ParameterValueConversionException;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.util.BillingUtil;
import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.data.util.ResultAdapter;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.InvoiceServiceHolder;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.rgw.tools.Result;
import ch.rgw.tools.Tree;

/**
 * Command um Rechnungen aus einer Liste von Patienten, Fällen und
 * Konsultationen zu erstellen Der Parameter des Commands muss ein Tree sein,
 * welcher in der ersten Ebene die Patienten, in der zweiten die Fälle und in
 * der Dritten die zu verrechnenden Konsultationen enthält.
 *
 * @author gerry
 *
 */
public class ErstelleRnnCommand extends AbstractHandler {
	public static final String ID = "bill.create"; //$NON-NLS-1$

	@SuppressWarnings("unchecked")
	public Object execute(ExecutionEvent eev) throws ExecutionException {
		Tree<?> tSelection = null;
		String px = eev.getParameter("ch.elexis.RechnungErstellen.parameter"); //$NON-NLS-1$
		try {
			tSelection = (Tree<?>) new TreeToStringConverter().convertToObject(px);
		} catch (ParameterValueConversionException pe) {
			throw new ExecutionException("Bad parameter " + pe.getMessage()); //$NON-NLS-1$
		}
		IProgressMonitor monitor = Handler.getMonitor(eev);
		Result<IInvoice> res = null;
		for (Tree tPat = tSelection.getFirstChild(); tPat != null; tPat = tPat.getNextSibling()) {
			int rejected = 0;
			for (Tree tFall = tPat.getFirstChild(); tFall != null; tFall = tFall.getNextSibling()) {
				Fall fall = (Fall) tFall.contents;
				if (ConfigServiceHolder.getUser(Preferences.LEISTUNGSCODES_BILLING_STRICT, true)) {
					if (!fall.isValid()) {
						rejected++;
						continue;
					}
				}
				Collection<Tree> lt = tFall.getChildren();

				List<Konsultation> toBill = new ArrayList<>(lt.size() + 1);
				for (Tree t : lt) {
					toBill.add((Konsultation) t.contents);
				}

				Map<Integer, List<IEncounter>> sortedByYears = BillingUtil
						.getSortedEncountersByYear(NoPoUtil.loadAsIdentifiable(toBill, IEncounter.class));
				if (!BillingUtil.canBillYears(new ArrayList<>(sortedByYears.keySet()))) {
					StringJoiner sj = new StringJoiner(", "); //$NON-NLS-1$
					sortedByYears.keySet().forEach(i -> sj.add(Integer.toString(i)));
					if (MessageDialog.openQuestion(Display.getDefault().getActiveShell(), "Rechnung Validierung",
							"Die Leistungen sind aus Jahren die nicht kombinierbar sind.\n\nWollen Sie separate Rechnungen für die Jahre "
									+ sj.toString() + " erstellen?")) {
						// bill each year separately
						for (Integer year : sortedByYears.keySet()) {
							res = InvoiceServiceHolder.get().invoice(sortedByYears.get(year));
							if (monitor != null) {
								monitor.worked(1);
							}
							if (!res.isOK()) {
								ErrorDialog.openError(HandlerUtil.getActiveShell(eev),
										Messages.KonsZumVerrechnenView_errorInInvoice,

										NLS.bind(Messages.KonsZumVerrechnenView_invoiceForCase,
												new Object[] { fall.getLabel(), fall.getPatient().getLabel() }),
										ResultAdapter.getResultAsStatus(res));
							} else {
								tPat.remove(tFall);
							}
						}
					}
				} else {
					res = InvoiceServiceHolder.get().invoice(NoPoUtil.loadAsIdentifiable(toBill, IEncounter.class));
					if (monitor != null) {
						monitor.worked(1);
					}
					if (!res.isOK()) {
						ErrorDialog.openError(HandlerUtil.getActiveShell(eev),
								Messages.KonsZumVerrechnenView_errorInInvoice,

								NLS.bind(Messages.KonsZumVerrechnenView_invoiceForCase,
										new Object[] { fall.getLabel(), fall.getPatient().getLabel() }),
								ResultAdapter.getResultAsStatus(res));
					} else {
						tPat.remove(tFall);
					}
				}
			}
			if (rejected != 0) {
				SWTHelper.showError(Messages.ErstelleRnnCommand_BadCaseDefinition,
						Integer.toString(rejected) + Messages.ErstelleRnnCommand_BillsNotCreatedMissingData
								+ Messages.ErstelleRnnCommand_ErstelleRnnCheckCaseDetails);
			} else {
				tSelection.remove(tPat);
			}
		}
		return res;
	}

	public static Object ExecuteWithParams(IViewSite origin, Tree<?> tSelection) {
		IHandlerService handlerService = (IHandlerService) origin.getService(IHandlerService.class);
		ICommandService cmdService = (ICommandService) origin.getService(ICommandService.class);
		try {
			Command command = cmdService.getCommand(ID);
			Parameterization px = new Parameterization(command.getParameter("ch.elexis.RechnungErstellen.parameter"), //$NON-NLS-1$
					new TreeToStringConverter().convertToString(tSelection));
			ParameterizedCommand parmCommand = new ParameterizedCommand(command, new Parameterization[] { px });

			return handlerService.executeCommand(parmCommand, null);

		} catch (Exception ex) {
			throw new RuntimeException("add.command not found"); //$NON-NLS-1$
		}
	}
}
