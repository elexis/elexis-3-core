/*******************************************************************************
 * Copyright (c) 2013 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.optifier;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.data.interfaces.IVerrechenbar.DefaultOptifier;
import ch.elexis.core.model.IDiagnose;
import ch.elexis.core.ui.dialogs.SelectFallNoObligationDialog;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.rgw.tools.Result;

public class NoObligationOptifier extends DefaultOptifier {
	
	private Fall noOblFall;
	
	@Override
	public Result<IVerrechenbar> add(IVerrechenbar code, Konsultation kons){
		String gesetz = kons.getFall().getConfiguredBillingSystemLaw().name();
		
		boolean forceObligation = CoreHub.userCfg.get(Preferences.LEISTUNGSCODES_OBLIGATION, false);
		
		if (forceObligation && gesetz.equalsIgnoreCase("KVG")) {
			noOblFall = null;
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run(){
					SelectFallNoObligationDialog dlg =
						new SelectFallNoObligationDialog(kons.getFall(), code);
					if (dlg.open() == Dialog.OK) {
						noOblFall = dlg.getFall();
					}
					
				}
			});
			
			if (noOblFall != null) {
				// check if there is a Konsultation in the selected Fall on the same date
				Konsultation noOblKons = getKonsFromFallByDate(noOblFall, kons.getDatum());
				// create new Konsultation if there is none matching
				if (noOblKons == null) {
					noOblKons = noOblFall.neueKonsultation();
					// transfer diagnoses to the Konsultation
					List<IDiagnose> diagnoses = kons.getDiagnosen();
					for (IDiagnose diag : diagnoses)
						noOblKons.addDiagnose(diag);
				}
				// add the no obligation IVerrechenbar to the new Konsultation
				noOblKons.addLeistung(code);
				// return ok
				return new Result<IVerrechenbar>(code);
			}
			return new Result<IVerrechenbar>(
				Result.SEVERITY.WARNING,
				0,
				"Auf diesen Fall können nur Pflichtleistungen verrechnet werden. Bitte einen separaten Fall für Nichtpflichtleistungen anlegen.",
				null, false);
		}
		
		return super.add(code, kons);
	}
	
	private Konsultation getKonsFromFallByDate(Fall fall, String date){
		Konsultation[] konsen = fall.getBehandlungen(false);
		for (int i = 0; i < konsen.length; i++) {
			Konsultation kons = konsen[i];
			if (kons.getDatum().equals(date)) {
				return kons;
			}
		}
		return null;
	}
}
