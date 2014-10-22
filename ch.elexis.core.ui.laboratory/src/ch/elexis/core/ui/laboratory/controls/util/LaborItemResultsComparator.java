package ch.elexis.core.ui.laboratory.controls.util;

import java.util.Comparator;

import ch.elexis.core.ui.laboratory.controls.model.LaborItemResults;
import ch.elexis.data.LabItem;

public class LaborItemResultsComparator implements Comparator<LaborItemResults> {
	
	@Override
	public int compare(LaborItemResults left, LaborItemResults right){
		LabItem leftItem = left.getFirstResult().getItem();
		LabItem rightItem = right.getFirstResult().getItem();
		
		try {
			Integer no1 = Integer.parseInt(leftItem.getPrio());
			Integer no2 = Integer.parseInt(rightItem.getPrio());
			
			return no1.compareTo(no2);
		} catch (NumberFormatException nfe) {
			return leftItem.getPrio().compareToIgnoreCase(rightItem.getPrio());
		}
	}
}
