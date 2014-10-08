package ch.elexis.core.ui.laboratory.controls.util;

import java.util.Comparator;

import ch.elexis.core.ui.laboratory.controls.model.LaborItemResults;
import ch.elexis.data.LabItem;

public class LaborItemResultsComparator implements Comparator<LaborItemResults> {
	
	@Override
	public int compare(LaborItemResults left, LaborItemResults right){
		LabItem leftItem = left.getFirstResult().getItem();
		LabItem rightItem = right.getFirstResult().getItem();
		
		return leftItem.getPrio().compareTo(rightItem.getPrio());
	}
	
}
