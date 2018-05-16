package ch.elexis.core.ui.laboratory.controls.util;

import java.util.Comparator;

import ch.elexis.core.data.interfaces.ILabItem;
import ch.elexis.core.ui.laboratory.controls.model.LaborItemResults;

public class LaborItemResultsComparator implements Comparator<LaborItemResults> {
	
	@Override
	public int compare(LaborItemResults left, LaborItemResults right){
		ILabItem leftItem = left.getFirstResult().getItem();
		ILabItem rightItem = right.getFirstResult().getItem();
		
		try {
			Integer no1 = Integer.parseInt(leftItem.getPriority());
			Integer no2 = Integer.parseInt(rightItem.getPriority());
			
			return no1.compareTo(no2);
		} catch (NumberFormatException nfe) {
			return leftItem.getPriority().compareToIgnoreCase(rightItem.getPriority());
		}
	}
}
