package ch.elexis.core.ui.medication.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.MedicationServiceHolder;
import ch.elexis.core.ui.medication.handlers.ApplyCustomSortingHandler;
import ch.elexis.data.Patient;
import ch.elexis.data.Prescription;
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
	
	public static String calculateDailyCostAsString(List<IPrescription> prescriptions){
		String TTCOST = Messages.FixMediDisplay_DailyCost;
		
		double cost = 0.0;
		boolean canCalculate = true;
		
		for (IPrescription prescription : prescriptions) {
			float num = MedicationServiceHolder.get().getDailyDosageAsFloat(prescription);
			try {
				IArticle article = prescription.getArticle();
				if (article != null) {
					int ve = article.getPackageSize();
					if (ve != 0) {
						Money price = article.getSellingPrice();
						cost += num * price.getAmount() / ve;
					} else {
						canCalculate = false;
					}
				} else {
					canCalculate = false;
				}
			} catch (Exception ex) {
				LoggerFactory.getLogger(MedicationViewHelper.class)
					.warn("Error calculating daily cost of prescription", ex);
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
	public static List<IArticle> getAllGtins(List<IPrescription> pres){
		ArrayList<IArticle> gtins = new ArrayList<>();
		for (IPrescription pr : pres) {
			IArticle art = pr.getArticle();
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
	 * @param patient
	 * @return
	 */
	public static List<IPrescription> loadInputData(boolean loadFullHistory, IPatient patient){
		if (patient == null)
			return Collections.emptyList();
			
		if (loadFullHistory) {
			return loadAllHistorical(patient);
		}
		return loadNonHistorical(patient);
	}
	
	private static List<IPrescription> loadNonHistorical(IPatient patient){
		if (patient != null) {
			List<IPrescription> tmpPrescs =
				patient.getMedication(Arrays.asList(EntryType.FIXED_MEDICATION,
					EntryType.RESERVE_MEDICATION, EntryType.SYMPTOMATIC_MEDICATION));
			
			List<IPrescription> result = new ArrayList<>();
			for (IPrescription p : tmpPrescs) {
				if (p.getArticle() != null && p.getArticle().getAtcCode() != null) {
					if (p.getArticle().getAtcCode().toUpperCase().startsWith("J07")) {
						continue;
					}
				}
				result.add(p);
			}
			return result;
		}
		return Collections.emptyList();
	}
	
	private static List<IPrescription> loadAllHistorical(IPatient patient){
		IQuery<IPrescription> query = CoreModelServiceHolder.get().getQuery(IPrescription.class);
		query.and(ModelPackage.Literals.IPRESCRIPTION__PATIENT, COMPARATOR.EQUALS, patient);
		query.orderBy(ModelPackage.Literals.IPRESCRIPTION__DATE_FROM, ORDER.DESC);
		return query.execute();
	}
}
