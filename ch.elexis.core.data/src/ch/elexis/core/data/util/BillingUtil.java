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
import java.util.Set;
import java.util.stream.Collectors;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.IDiagnose;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Mandant;
import ch.elexis.data.Query;
import ch.elexis.data.Rechnung;
import ch.elexis.data.Rechnungssteller;
import ch.elexis.data.Verrechnet;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;
import ch.rgw.tools.Result.SEVERITY;
import ch.rgw.tools.TimeTool;

/**
 * Util class with methods for checking and preparing {@link Konsultation}, with the goal to include
 * them in a bill {@link Rechnung#build(List)}.
 * 
 * @author thomas
 *
 */
public class BillingUtil {
	
	public static String BILLINGCHECK_ENABLED_CFG = "ch.elexis.core.data/billablecheck/";
	
	/**
	 * Interface definition for checking a {@link Konsultation} if it can be included on a bill.
	 * 
	 */
	public static interface IBillableCheck {
		/**
		 * Get a unique id of the check.
		 * 
		 * @return
		 */
		public String getId();
		
		/**
		 * Get a human readable description of the check.
		 * 
		 * @return
		 */
		public String getDescription();
		
		/**
		 * Test if the {@link Konsultation} is bill able. If no the error is added to the
		 * {@link Result}.
		 * 
		 * @param konsultation
		 * @param result
		 * @return
		 */
		public boolean isBillable(Konsultation konsultation, Result<Konsultation> result);
	}
	
	/**
	 * Array of {@link IBillableCheck} implementations. Implementations can be disabled or enabled
	 * using {@link BillingUtil#setCheckEnabled(IBillableCheck, boolean)}.
	 * 
	 */
	public static IBillableCheck[] billableChecks = {
		// Check for zero sales.
		new IBillableCheck() {
			@Override
			public boolean isBillable(Konsultation konsultation, Result<Konsultation> result){
				boolean fail = getTotal(konsultation).isZero();
				if (fail) {
					result.add(SEVERITY.ERROR, 1, getDescription(), konsultation, false);
				}
				return !fail;
			}
			
			@Override
			public String getId(){
				return "zeroSales";
			}

			@Override
			public String getDescription(){
				return "Behandlung mit Umsatz 0";
			}
		},
		// Check for invalid Mandant.
		new IBillableCheck() {
			@Override
			public boolean isBillable(Konsultation konsultation, Result<Konsultation> result){
				Mandant mandant = konsultation.getMandant();
				boolean fail = (mandant == null || !mandant.isValid());
				if (fail) {
					result.add(SEVERITY.ERROR, 1, getDescription(), konsultation, false);
				}
				return !fail;
			}
			
			@Override
			public String getId(){
				return "invalidMandant";
			}
			
			@Override
			public String getDescription(){
				return "Ungültiger Mandant";
			}
		},
		// Check for missing coverage.
		new IBillableCheck() {
			@Override
			public boolean isBillable(Konsultation konsultation, Result<Konsultation> result){
				Fall fall = konsultation.getFall();
				boolean fail = (fall == null);
				if (fail) {
					result.add(SEVERITY.ERROR, 1, getDescription(), konsultation, false);
				}
				return !fail;
			}
			
			@Override
			public String getId(){
				return "noCoverage";
			}
			
			@Override
			public String getDescription(){
				return "Fehlender Fall";
			}
		},
		// Check for invalid coverage.
		new IBillableCheck() {
			@Override
			public boolean isBillable(Konsultation konsultation, Result<Konsultation> result){
				Fall fall = konsultation.getFall();
				boolean fail = (fall != null
					&& CoreHub.userCfg.get(Preferences.LEISTUNGSCODES_BILLING_STRICT, true)
					&& !fall.isValid());
				if (fail) {
					result.add(SEVERITY.ERROR, 1, getDescription(), konsultation, false);
				}
				return !fail;
			}
			
			@Override
			public String getId(){
				return "invalidCoverage";
			}
			
			@Override
			public String getDescription(){
				return "Fall nicht gültig";
			}
		},
		// Check for missing diagnose.
		new IBillableCheck() {
			@Override
			public boolean isBillable(Konsultation konsultation, Result<Konsultation> result){
				ArrayList<IDiagnose> diagnosen = konsultation.getDiagnosen();
				boolean fail = (diagnosen == null || diagnosen.isEmpty());
				if (fail) {
					result.add(SEVERITY.ERROR, 1, getDescription(), konsultation, false);
				}
				return !fail;
			}
			
			@Override
			public String getId(){
				return "noDiagnose";
			}
			
			@Override
			public String getDescription(){
				return "Keine Diagnose";
			}
		},
		// Check for invalid date.
		new IBillableCheck() {
			private TimeTool checkTool = new TimeTool();
			
			@Override
			public boolean isBillable(Konsultation konsultation, Result<Konsultation> result){
				boolean fail = (checkTool.set(konsultation.getDatum()) == false);
				if (fail) {
					result.add(SEVERITY.ERROR, 1, getDescription(), konsultation, false);
				}
				return !fail;
			}
			
			@Override
			public String getId(){
				return "invalidDate";
			}
			
			@Override
			public String getDescription(){
				return "Ungültiges Datum";
			}
		},
		// Check for missing diagnose in open Konsultation series. 
		new IBillableCheck() {
			@Override
			public boolean isBillable(Konsultation konsultation, Result<Konsultation> result){
				boolean fail = false;
				ArrayList<IDiagnose> diagnosen = konsultation.getDiagnosen();
				if (diagnosen == null || diagnosen.isEmpty()) {
					fail = true;
					// get other open konsultation of the case
					Query<Konsultation> query = new Query<>(Konsultation.class);
					query.add(Konsultation.FLD_BILL_ID, Query.EQUALS, null);
					query.add(Konsultation.FLD_CASE_ID, Query.EQUALS,
						konsultation.getFall().getId());
					List<Konsultation> openKonsultationen = query.execute();
					for (Konsultation openKons : openKonsultationen) {
						ArrayList<IDiagnose> diag = openKons.getDiagnosen();
						if (diag != null && !diag.isEmpty()) {
							fail = false;
							break;
						}
					}
					if (fail) {
						result.add(SEVERITY.ERROR, 1, getDescription(), konsultation, false);
					}
				}
				return !fail;
			}
			
			@Override
			public String getId(){
				return "noDiagnoseInSeries";
			}
			
			@Override
			public String getDescription(){
				return "Keine Diagnose in der Behandlungsserie";
			}
		}
	};
	
	public static boolean isCheckEnabled(IBillableCheck check){
		return CoreHub.globalCfg.get(BILLINGCHECK_ENABLED_CFG + check.getId(), true);
	}
	
	public static void setCheckEnabled(IBillableCheck check, boolean enabled){
		CoreHub.globalCfg.set(BILLINGCHECK_ENABLED_CFG + check.getId(), enabled);
	}
	
	/**
	 * Test if the {@link Konsultation} can be billed, and return a {@link Result} containing
	 * possible error messages. {@link IBillableCheck} are applied if enabled.
	 * 
	 * @param konsultation
	 * @return
	 */
	public static Result<Konsultation> getBillableResult(Konsultation konsultation){
		
		Result<Konsultation> result = new Result<>(konsultation);
		
		for (IBillableCheck iBillableCheck : billableChecks) {
			if (isCheckEnabled(iBillableCheck)) {
				iBillableCheck.isBillable(konsultation, result);
			}
		}
		return result;
	}
	
	/**
	 * Calculate the total amount of all {@link Verrechnet} of the {@link Konsultation}.
	 * 
	 * @param konsultation
	 * @return
	 */
	public static Money getTotal(Konsultation konsultation){
		Money total = new Money(0);
		List<Verrechnet> leistungen = konsultation.getLeistungen();
		for (Verrechnet verrechnet : leistungen) {
			total.addMoney(verrechnet.getNettoPreis());
		}
		return total;
	}
	
	/**
	 * Remove all not bill able {@link Konsultation} from the provided {@link List}.
	 * 
	 * @param konsultationen
	 * @return filtered {@link List}
	 */
	public static List<Konsultation> filterNotBillable(List<Konsultation> konsultationen){
		return konsultationen.parallelStream().filter(k -> getBillableResult(k).isOK())
			.collect(Collectors.toList());
	}
	
	/**
	 * Get a Map representation of bill able {@link Konsultation} instances. To be bill able the
	 * list of {@link Konsultation} is split by {@link Rechnungssteller} and {@link Fall}.
	 * 
	 * @param konsultationen
	 * @return map sorted by billing criteria
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
	
	/**
	 * Create bills {@link Rechnung} for all {@link Konsultation} contained in the map. Returns al
	 * list with the {@link Result} of building the bills.
	 * 
	 * @param toBillMap
	 * @return
	 */
	public static List<Result<Rechnung>> createBills(
		Map<Rechnungssteller, Map<Fall, List<Konsultation>>> toBillMap){
		List<Result<Rechnung>> ret = new ArrayList<>();
		Set<Rechnungssteller> invoicers = toBillMap.keySet();
		for (Rechnungssteller invoicer : invoicers) {
			Set<Fall> faelle = toBillMap.get(invoicer).keySet();
			for (Fall fall : faelle) {
				ret.add(Rechnung.build(toBillMap.get(invoicer).get(fall)));
			}
		}
		return ret;
	}
}
