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
package ch.elexis.core.data.util;

import java.util.ArrayList;
import java.util.List;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.IDiagnose;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Mandant;
import ch.elexis.data.Rechnung;
import ch.elexis.data.Verrechnet;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;
import ch.rgw.tools.Result.SEVERITY;
import ch.rgw.tools.TimeTool;

/**
 * Util class with methods for checking and creating {@link Rechnung} and {@link Konsultation}.
 * 
 * @author thomas
 *
 */
public class BillingUtil {
	
	public static Result<Konsultation> check(Konsultation konsultation){
		TimeTool checkTool = new TimeTool();
		Result<Konsultation> result = new Result<>(konsultation);
		if (getTotal(konsultation).isZero()) {
			result.add(SEVERITY.ERROR, 1, "Behandlung mit Umsatz 0", konsultation, false);
		}
		Mandant mandant = konsultation.getMandant();
		if (mandant == null || !mandant.isValid()) {
			result.add(SEVERITY.ERROR, 1, "Ungültiger Mandant", konsultation, false);
		}
		Fall fall = konsultation.getFall();
		if (fall == null) {
			result.add(SEVERITY.ERROR, 1, "Fehlender Fall", konsultation, false);
		}
		if (fall != null && CoreHub.userCfg.get(Preferences.LEISTUNGSCODES_BILLING_STRICT, false)
			&& !fall.isValid()) {
			result.add(SEVERITY.ERROR, 1, "Fall nicht gültig", konsultation, false);
		}
		ArrayList<IDiagnose> diagnosen = konsultation.getDiagnosen();
		if (diagnosen == null || diagnosen.isEmpty()) {
			result.add(SEVERITY.ERROR, 1, "Keine Diagnose", konsultation, false);
		}
		if (checkTool.set(konsultation.getDatum()) == false) {
			result.add(SEVERITY.ERROR, 1, "Ungültiges Datum", konsultation, false);
		}
		return result;
	}
	
	public static Money getTotal(Konsultation konsultation){
		Money total = new Money(0);
		List<Verrechnet> leistungen = konsultation.getLeistungen();
		for (Verrechnet verrechnet : leistungen) {
			total.addMoney(verrechnet.getNettoPreis());
		}
		return total;
	}
}
