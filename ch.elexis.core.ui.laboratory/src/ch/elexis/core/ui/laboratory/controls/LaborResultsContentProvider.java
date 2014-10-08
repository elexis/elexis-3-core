package ch.elexis.core.ui.laboratory.controls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.elexis.core.ui.laboratory.controls.model.LaborItemResults;
import ch.elexis.core.ui.laboratory.controls.util.LaborItemResultsComparator;
import ch.elexis.data.LabResult;
import ch.rgw.tools.TimeTool;

class LaborResultsContentProvider implements ITreeContentProvider {
	
	private HashMap<String, HashMap<String, HashMap<String, List<LabResult>>>> grouped;
	private ArrayList<String> groups = new ArrayList<String>();
	
	private HashMap<String, LaborItemResults> itemResults = new HashMap<String, LaborItemResults>();
	
	private HashSet<String> dates = new HashSet<String>();
	
	public List<TimeTool> getDates(){
		ArrayList<TimeTool> ret = new ArrayList<TimeTool>();
		for (String date : dates) {
			ret.add(new TimeTool(date));
		}
		Collections.sort(ret);
		return ret;
	}
	
	@Override
	public void dispose(){
		// TODO Auto-generated method stub
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
		if (newInput instanceof HashMap<?, ?>) {
			grouped = (HashMap<String, HashMap<String, HashMap<String, List<LabResult>>>>) newInput;
			updateItemResults();
		}
	}
	
	private void updateItemResults(){
		groups.clear();
		itemResults.clear();
		dates.clear();
		for (String group : grouped.keySet()) {
			HashMap<String, HashMap<String, List<LabResult>>> itemMap = grouped.get(group);
			for (String item : itemMap.keySet()) {
				LaborItemResults results = new LaborItemResults(item, itemMap.get(item));
				if (results.isVisible()) {
					itemResults.put(item, results);
					dates.addAll(results.getDays());
				}
			}
		}
		groups.addAll(grouped.keySet());
		Collections.sort(groups);
	}
	
	@Override
	public Object[] getElements(Object inputElement){
		return groups.toArray();
	}
	
	@Override
	public Object[] getChildren(Object parentElement){
		if (parentElement instanceof String) {
			HashMap<String, HashMap<String, List<LabResult>>> itemMap = grouped.get(parentElement);
			ArrayList<LaborItemResults> ret = new ArrayList<LaborItemResults>();
			for (String item : itemMap.keySet()) {
				if (itemResults.get(item) != null) {
					ret.add(itemResults.get(item));
				}
			}
			Collections.sort(ret, new LaborItemResultsComparator());
			return ret.toArray();
		}
		return null;
	}
	
	@Override
	public Object getParent(Object element){
		return null;
	}
	
	@Override
	public boolean hasChildren(Object element){
		if (element instanceof String) {
			return grouped.get(element) != null && !grouped.get(element).isEmpty();
		}
		return false;
	}
}