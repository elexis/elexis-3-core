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

import java.text.ParseException;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.model.InvoiceState;
import ch.elexis.data.Mandant;
import ch.elexis.data.Query;
import ch.elexis.data.Rechnung;
import ch.elexis.data.Rechnungssteller;
import ch.rgw.io.Settings;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;

public class MahnlaufCommand extends AbstractHandler {
	private static final String STR_STATUS_DATUM = "StatusDatum"; //$NON-NLS-1$
	private static final String STR_MANDANT_I_D = "MandantID"; //$NON-NLS-1$
	private static final String STR_RN_STATUS = "RnStatus"; //$NON-NLS-1$
	public final static String ID = "bill.reminder"; //$NON-NLS-1$

	private Settings rnsSettings;

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		Mandant mandant = (Mandant) ElexisEventDispatcher.getSelected(Mandant.class);
		Rechnungssteller rechnungssteller = mandant.getRechnungssteller();
		rnsSettings = CoreHub.getUserSetting(rechnungssteller);

		Query<Mandant> qbe = new Query<>(Mandant.class);
		List<Mandant> allMandants = qbe.execute();

		for (Mandant m : allMandants) {
			if (m.getRechnungssteller().equals(rechnungssteller))
				performMahnlaufForMandant(m.getId());
		}
		return null;
	}

	private void performMahnlaufForMandant(String mandantId) {
		Query<Rechnung> qbe = new Query<>(Rechnung.class);
		qbe.add(STR_RN_STATUS, "=", Integer.toString(InvoiceState.OPEN_AND_PRINTED.getState())); //$NON-NLS-1$
		qbe.add(STR_MANDANT_I_D, "=", mandantId); //$NON-NLS-1$

		TimeTool tt = new TimeTool();
		// Rechnung zu 1. Mahnung
		int days = rnsSettings.get(Preferences.RNN_DAYSUNTIL1ST, 30);
		Money betrag = new Money();
		try {
			betrag = new Money(rnsSettings.get(Preferences.RNN_AMOUNT1ST, "0.00")); //$NON-NLS-1$
		} catch (ParseException ex) {
			ExHandler.handle(ex);

		}
		tt.addHours(days * 24 * -1);
		qbe.add(STR_STATUS_DATUM, "<", tt.toString(TimeTool.DATE_COMPACT)); //$NON-NLS-1$
		List<Rechnung> list = qbe.execute();
		for (Rechnung rn : list) {
			rn.setStatus(InvoiceState.DEMAND_NOTE_1);
			if (!betrag.isZero()) {
				rn.addZahlung(new Money(betrag).multiply(-1.0), ch.elexis.data.Messages.Rechnung_Mahngebuehr1, null);
			}
		}
		// 1. Mahnung zu 2. Mahnung
		qbe.clear();
		qbe.add(STR_RN_STATUS, "=", Integer.toString(InvoiceState.DEMAND_NOTE_1_PRINTED.getState())); //$NON-NLS-1$
		qbe.add(STR_MANDANT_I_D, "=", mandantId); //$NON-NLS-1$
		tt = new TimeTool();
		days = rnsSettings.get(Preferences.RNN_DAYSUNTIL2ND, 10);
		try {
			betrag = new Money(rnsSettings.get(Preferences.RNN_AMOUNT2ND, "0.00")); //$NON-NLS-1$
		} catch (ParseException ex) {
			ExHandler.handle(ex);
			betrag = new Money();
		}
		tt.addHours(days * 24 * -1);
		qbe.add(STR_STATUS_DATUM, "<", tt.toString(TimeTool.DATE_COMPACT)); //$NON-NLS-1$
		list = qbe.execute();
		for (Rechnung rn : list) {
			rn.setStatus(InvoiceState.DEMAND_NOTE_2);
			if (!betrag.isZero()) {
				rn.addZahlung(new Money(betrag).multiply(-1.0), ch.elexis.data.Messages.Rechnung_Mahngebuehr2, null);
			}
		}
		// 2. Mahnung zu 3. Mahnung
		qbe.clear();
		qbe.add(STR_RN_STATUS, "=", Integer.toString(InvoiceState.DEMAND_NOTE_2_PRINTED.getState())); //$NON-NLS-1$
		qbe.add(STR_MANDANT_I_D, "=", mandantId); //$NON-NLS-1$
		tt = new TimeTool();
		days = rnsSettings.get(Preferences.RNN_DAYSUNTIL3RD, 10);
		try {
			betrag = new Money(rnsSettings.get(Preferences.RNN_AMOUNT3RD, "0.00")); //$NON-NLS-1$
		} catch (ParseException ex) {
			ExHandler.handle(ex);
			betrag = new Money();
		}
		tt.addHours(days * 24 * -1);
		qbe.add(STR_STATUS_DATUM, "<", tt.toString(TimeTool.DATE_COMPACT)); //$NON-NLS-1$
		list = qbe.execute();
		for (Rechnung rn : list) {
			rn.setStatus(InvoiceState.DEMAND_NOTE_3);
			if (!betrag.isZero()) {
				rn.addZahlung(new Money(betrag).multiply(-1.0), ch.elexis.data.Messages.Rechnung_Mahngebuehr3, null);
			}
		}
	}
}
