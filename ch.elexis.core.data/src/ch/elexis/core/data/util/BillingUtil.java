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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.IDiagnose;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Mandant;
import ch.elexis.data.Rechnung;
import ch.elexis.data.Rechnungssteller;
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
	
	public static Result<Konsultation> getBillableResult(Konsultation konsultation){
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
		if (fall != null && CoreHub.userCfg.get(Preferences.LEISTUNGSCODES_BILLING_STRICT, true)
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
	
	public static List<Konsultation> filterNotBillable(List<Konsultation> konsultationen){
		return konsultationen.parallelStream().filter(k -> getBillableResult(k).isOK())
			.collect(Collectors.toList());
	}
	
	/**
	 * Get a Map representation of bill able {@link Konsultation} instances. To be bill able the
	 * lists of {@link Konsultation} is split by {@link Rechnungssteller} and {@link Fall}.
	 * 
	 * @param konsultationen
	 * @return
	 */
	public static Map<Rechnungssteller, Map<Fall, List<Konsultation>>> getGroupedBillable(
		List<Konsultation> konsultationen){
		HashMap<Rechnungssteller, Map<Fall, List<Konsultation>>> ret = new HashMap<>();
		for (Konsultation konsultation : konsultationen) {
			Rechnungssteller invoicer = konsultation.getMandant().getRechnungssteller();
			Map<Fall, List<Konsultation>> fallMap = ret.get(invoicer);
			if (fallMap == null) {
				fallMap = new HashMap<>();
			}
			List<Konsultation> list = fallMap.get(konsultation.getFall());
			if (list == null) {
				list = new ArrayList<>();
			}
			list.add(konsultation);
			fallMap.put(konsultation.getFall(), list);
			ret.put(invoicer, fallMap);
		}
		return ret;
	}
}
