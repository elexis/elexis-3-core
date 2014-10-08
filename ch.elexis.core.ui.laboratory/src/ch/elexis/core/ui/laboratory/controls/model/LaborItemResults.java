package ch.elexis.core.ui.laboratory.controls.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ch.elexis.data.LabItem;
import ch.elexis.data.LabResult;

public class LaborItemResults implements Comparable<LaborItemResults> {
	private HashMap<String, List<LabResult>> results;
	private String item;
	
	public LaborItemResults(String item, HashMap<String, List<LabResult>> results){
		this.results = results;
		this.item = item;
	}
	
	public LabItem getLabItem(){
		return getFirstResult().getItem();
	}

	public String getItem(){
		return item;
	}
	
	public boolean isVisible(){
		return results.values().iterator().next().get(0).getItem().isVisible();
	}
	
	public LabResult getFirstResult(){
		return results.values().iterator().next().get(0);
	}
	
	public List<LabResult> getResult(String date){
		return results.get(date);
	}
	
	public List<String> getDays(){
		return new ArrayList<String>(results.keySet());
	}
	
	@Override
	public int compareTo(LaborItemResults o){
		return item.compareTo(o.getItem());
	}
}
