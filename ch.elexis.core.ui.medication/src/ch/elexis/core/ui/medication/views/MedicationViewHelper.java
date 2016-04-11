package ch.elexis.core.ui.medication.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.elexis.data.Artikel;
import ch.elexis.data.Prescription;
import ch.elexis.data.Query;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;

public class MedicationViewHelper {
	private static final int FILTER_PRESCRIPTION_AFTER_N_DAYS = 30;
	
	public static String calculateDailyCostAsString(List<Prescription> pres){
		String TTCOST = Messages.FixMediDisplay_DailyCost;
		
		double cost = 0.0;
		boolean canCalculate = true;
		
		for (Prescription pr : pres) {
			float num = Prescription.calculateTagesDosis(pr.getDosis());
			try {
				Artikel art = pr.getArtikel();
				if (art != null) {
					int ve = art.guessVE();
					if (ve != 0) {
						Money price = pr.getArtikel().getVKPreis();
						cost += num * price.getAmount() / ve;
					} else {
						canCalculate = false;
					}
				} else {
					canCalculate = false;
				}
			} catch (Exception ex) {
				ExHandler.handle(ex);
				canCalculate = false;
			}
		}
		
		double rounded = Math.round(100.0 * cost) / 100.0;
		if (canCalculate) {
			return TTCOST +" "+Double.toString(rounded);
		} else {
			if (rounded == 0.0) {
				return TTCOST + " ?";
			} else {
				return TTCOST + " >" + Double.toString(rounded);
			}
		}
	}
	
	/**
	 * <pre>
	 * SELECT * FROM PATIENT_ARTIKEL_JOINT 
	 * WHERE deleted='0' AND PatientId='C7dc8b102d96407ed0632' 
	 * AND (
	 * 	(DateFrom >= '20150922' AND RezeptID is not null)
	 * 	OR
	 * 	# FIXED MEDICATION
	 * 	(RezeptID is null AND DateUntil is null)
	 * )
	 * </pre>
	 * 
	 * @param patId
	 * @return
	 */
	public static List<Prescription> loadInputData(boolean loadFullHistory, String patId){
		if (patId == null)
			return Collections.emptyList();
			
		if (loadFullHistory) {
			return loadAllHistorical(patId);
		}
		return loadNonHistorical(patId);
	}
	
	private static List<Prescription> loadNonHistorical(String patId){
		TimeTool now = new TimeTool();
		TimeTool thirtyDaysAgo = new TimeTool();
		thirtyDaysAgo.addDays(-FILTER_PRESCRIPTION_AFTER_N_DAYS);
		//SELECT * FROM PATIENT_ARTIKEL_JOINT WHERE deleted='0' AND PatientId='C7dc8b102d96407ed0632' 
		Query<Prescription> qbe = new Query<Prescription>(Prescription.class);
		qbe.add(Prescription.FLD_PATIENT_ID, Query.EQUALS, patId);
		
		qbe.startGroup();
		//(DateFrom >= '20150922' AND RezeptID is not null)
		qbe.startGroup();
		qbe.add(Prescription.FLD_DATE_FROM, Query.GREATER_OR_EQUAL,
			thirtyDaysAgo.toString(TimeTool.TIMESTAMP));
		qbe.add(Prescription.FLD_REZEPT_ID, "not", null);
		qbe.endGroup();
		qbe.or();
		//(RezeptID is null AND DateUntil is null)
		qbe.startGroup();
		qbe.add(Prescription.FLD_REZEPT_ID, Query.EQUALS, null);
		qbe.startGroup();
		qbe.add(Prescription.FLD_DATE_UNTIL, Query.EQUALS, null);
		qbe.or();
		qbe.add(Prescription.FLD_DATE_UNTIL, Query.GREATER_OR_EQUAL,
			now.toString(TimeTool.TIMESTAMP));
		qbe.endGroup();
		qbe.endGroup();
		qbe.endGroup();
		
		List<Prescription> tmpPrescs = qbe.execute();
		
		List<Prescription> result = new ArrayList<Prescription>();
		for (Prescription p : tmpPrescs) {
			if (p.getArtikel() != null && p.getArtikel().getATC_code() != null) {
				if (p.getArtikel().getATC_code().toUpperCase().startsWith("J07"))
					continue;
			}
			
			result.add(p);
		}
		return result;
	}
	
	private static List<Prescription> loadAllHistorical(String patId){
		Query<Prescription> qbe = new Query<Prescription>(Prescription.class);
		qbe.add(Prescription.FLD_PATIENT_ID, Query.EQUALS, patId);
		return qbe.execute();
	}
}
