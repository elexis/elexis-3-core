package ch.elexis.core.ui.medication.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import ch.elexis.data.Prescription;

public enum ComboViewerSortOrder {
	MANUAL("manuell", 0, new ManualViewerComparator()), DEFAULT("standard", 1,
		new DefaultViewerComparator());
	
	final String label;
	final int val;
	final ViewerComparator vc;
	
	private ComboViewerSortOrder(String label, int val, ViewerComparator vc){
		this.label = label;
		this.val = val;
		this.vc = vc;
	}
	
	/**
	 * sort the medication order by manual ordering as stored in {@link Prescription#FLD_SORT_ORDER}
	 */
	public static class ManualViewerComparator extends ViewerComparator {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2){
			Prescription p1 = (Prescription) e1;
			Prescription p2 = (Prescription) e2;
			
			String sos1 = p1.get(Prescription.FLD_SORT_ORDER);
			String sos2 = p2.get(Prescription.FLD_SORT_ORDER);
			
			if (sos1.length() == 0 && sos2.length() == 0)
				return 0;
			
			int val1 = Integer.MAX_VALUE;
			int val2 = Integer.MAX_VALUE;
			
			try {
				val1 = Integer.parseInt(sos1);
			} catch (NumberFormatException nfe) {}
			
			try {
				val2 = Integer.parseInt(sos2);
			} catch (NumberFormatException nfe) {}
			
			return Integer.compare(val1, val2);
		}
	}
	
	/**
	 * sort the medication table viewer first by group (fixed medication or pro re nata medication),
	 * and then by natural article name
	 */
	public static class DefaultViewerComparator extends ViewerComparator {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2){
			Prescription p1 = (Prescription) e1;
			Prescription p2 = (Prescription) e2;
			
			boolean p1F = p1.isFixedMediation();
			boolean p2F = p2.isFixedMediation();
			
			if (p1F != p2F) {
				// they are not in the same group
				// so we sort by group first
				return (p1F) ? -1 : 1;
			}
			
			String l1 = p1.getLabel().toLowerCase();
			String l2 = p2.getLabel().toLowerCase();
			
			return l1.compareTo(l2);
		}
	}
	
	/**
	 * 
	 * @param i
	 * @return the respective {@link ComboViewerSortOrder} for i, or
	 *         {@link ComboViewerSortOrder#DEFAULT} if invalid or not found
	 */
	public static ComboViewerSortOrder getSortOrderPerValue(int i){
		for (ComboViewerSortOrder cvso : ComboViewerSortOrder.values()) {
			if (cvso.val == i)
				return cvso;
		}
		return null;
	}
}
