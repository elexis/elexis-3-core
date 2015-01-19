package ch.elexis.core.ui.medication.views;

import java.util.List;

import ch.elexis.data.Artikel;
import ch.elexis.data.Prescription;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Money;

public class MedicationViewHelper {
	
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
}
