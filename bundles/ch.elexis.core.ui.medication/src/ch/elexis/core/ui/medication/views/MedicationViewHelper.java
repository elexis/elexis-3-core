package ch.elexis.core.ui.medication.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.ui.medication.handlers.ApplyCustomSortingHandler;
import ch.elexis.data.Artikel;
import ch.elexis.data.Patient;
import ch.elexis.data.Prescription;
import ch.elexis.data.Query;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Money;

public class MedicationViewHelper {
	private static final int FILTER_PRESCRIPTION_AFTER_N_DAYS = 30;
	
	public static ViewerSortOrder getSelectedComparator(){
		ICommandService service =
			(ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		Command command = service.getCommand(ApplyCustomSortingHandler.CMD_ID);
		State state = command.getState(ApplyCustomSortingHandler.STATE_ID);
		
		if ((Boolean) state.getValue()) {
			return ViewerSortOrder.getSortOrderPerValue(ViewerSortOrder.MANUAL.val);
		} else {
			return ViewerSortOrder.getSortOrderPerValue(ViewerSortOrder.DEFAULT.val);
		}
	}
	
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
	 * 
	 * @param pres alist of prescriptions
	 * @return an ArrayList of unique GTIN of all prescribed articles
	 */
	public static ArrayList<Artikel> getAllGtins(List<Prescription> pres){
		ArrayList<Artikel> gtins = new ArrayList<Artikel>();
		for (Prescription pr : pres) {
			Artikel art = pr.getArtikel();
			if (art != null) {
				if (!gtins.contains(art)) {
					gtins.add(art);
				}
			}
		}
		return gtins;
	}
	
	/**
	 * Load the {@link Prescription} for the {@link Patient} referenced by patId. If the
	 * loadFullHistory parameter is false, a list of current active {@link Prescription} is
	 * returned.
	 * 
	 * @param loadFullHistory
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
		List<Prescription> tmpPrescs = Patient.load(patId).getMedication(EntryType.FIXED_MEDICATION,
			EntryType.RESERVE_MEDICATION, EntryType.SYMPTOMATIC_MEDICATION);
		
		List<Prescription> result = new ArrayList<Prescription>();
		for (Prescription p : tmpPrescs) {
			if (p.getArtikel() != null && p.getArtikel().getATC_code() != null) {
				if (p.getArtikel().getATC_code().toUpperCase().startsWith("J07")) {
					continue;
				}
			}
			result.add(p);
		}
		return result;
	}
	
	private static List<Prescription> loadAllHistorical(String patId){
		// prefetch the values needed for filter operations
		Query<Prescription> qbe = new Query<Prescription>(Prescription.class, null, null,
			Prescription.TABLENAME, new String[] {
				Prescription.FLD_DATE_FROM, Prescription.FLD_DATE_UNTIL, Prescription.FLD_REZEPT_ID,
				Prescription.FLD_PRESC_TYPE, Prescription.FLD_ARTICLE
			});
		qbe.add(Prescription.FLD_PATIENT_ID, Query.EQUALS, patId);
		qbe.orderBy(true, Prescription.FLD_DATE_FROM);
		return qbe.execute();
	}
}
