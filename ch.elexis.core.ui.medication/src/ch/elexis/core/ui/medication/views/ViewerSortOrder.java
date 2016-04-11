package ch.elexis.core.ui.medication.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

import ch.elexis.data.Prescription;
import ch.rgw.tools.TimeTool;

public enum ViewerSortOrder {
		MANUAL("manuell", 0, new ManualViewerComparator()),
		DEFAULT("standard", 1, new DefaultViewerComparator());
		
	final String label;
	final int val;
	final ViewerComparator vc;
	
	private static final int DESCENDING = 1;
	private static int direction = DESCENDING;
	private static int propertyIdx = 0;
	private static TimeTool time1 = new TimeTool();
	private static TimeTool time2 = new TimeTool();
	
	private ViewerSortOrder(String label, int val, ViewerComparator vc){
		this.label = label;
		this.val = val;
		this.vc = vc;
	}
	
	public int getDirection(){
		return direction == 1 ? SWT.DOWN : SWT.UP;
	}
	
	public void setColumn(int column){
		if (column == this.propertyIdx) {
			// Same column as last sort; toggle the direction
			direction = 1 - direction;
		} else {
			// New column; do an ascending sort
			this.propertyIdx = column;
			direction = DESCENDING;
		}
	}
	
	/**
	 * sort the medication order by manual ordering as stored in {@link Prescription#FLD_SORT_ORDER}
	 */
	public static class ManualViewerComparator extends ViewerComparator {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2){
			MedicationTableViewerItem p1 = (MedicationTableViewerItem) e1;
			MedicationTableViewerItem p2 = (MedicationTableViewerItem) e2;
			
			String sos1 = p1.getOrder();
			String sos2 = p2.getOrder();
			
			if (sos1.length() == 0 && sos2.length() == 0)
				return 0;
				
			int val1 = Integer.MAX_VALUE;
			int val2 = Integer.MAX_VALUE;
			
			try {
				val1 = Integer.parseInt(sos1);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
			
			try {
				val2 = Integer.parseInt(sos2);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
			
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
			MedicationTableViewerItem p1 = (MedicationTableViewerItem) e1;
			MedicationTableViewerItem p2 = (MedicationTableViewerItem) e2;
			int rc = 0;
			switch (propertyIdx) {
			case 0:
				rc = 0;
				break;
			case 1:
				String l1 = p1.getArtikelLabel();
				String l2 = p2.getArtikelLabel();
				if (l1 == null) {
					l1 = "";
				}
				if (l2 == null) {
					l2 = "";
				}
				rc = l1.compareTo(l2);
				break;
			case 2:
				String dose1 = p1.getDosis();
				String dose2 = p2.getDosis();
				rc = dose1.compareTo(dose2);
				break;
			case 3:
				time1.set(p1.getBeginDate());
				time2.set(p2.getBeginDate());
				rc = time1.compareTo(time2);
				break;
			case 4:
				String supUntil1 = getSuppliedUntil(p1);
				String supUntil2 = getSuppliedUntil(p2);
				rc = supUntil1.compareTo(supUntil2);
				break;
			case 5:
				// stopped column is optional 
				boolean stop1IsValid = isStopped(p1.getEndDate());
				boolean stop2IsValid = isStopped(p2.getEndDate());
				
				if (stop1IsValid && stop2IsValid) {
					time1.set(p1.getEndDate());
					time2.set(p2.getEndDate());
					rc = time1.compareTo(time2);
				} else {
					if (stop1IsValid && !stop2IsValid)
						rc = -1;
					else if (!stop1IsValid && stop2IsValid)
						rc = 1;
					else
						rc = 0;
				}
				break;
			case 6:
				String com1 = p1.getBemerkung();
				String com2 = p2.getBemerkung();
				rc = com1.compareTo(com2);
				break;
			case 7:
				String stopReason1 = p1.getStopReason();
				if (stopReason1 == null)
					stopReason1 = "";
					
				String stopReason2 = p2.getStopReason();
				if (stopReason2 == null)
					stopReason2 = "";
					
				rc = stopReason1.compareTo(stopReason2);
				break;
			default:
				rc = 0;
			}
			// If descending order, flip the direction
			if (direction == DESCENDING) {
				rc = -rc;
			}
			return rc;
		}
		
		private String getSuppliedUntil(MedicationTableViewerItem p){
			// !A OR B === !(A AND !B)
			boolean val = p.isFixedMediation() && !(p.isReserveMedication());
			if (!val) {
				return "";
			}
			
			TimeTool time = p.getSuppliedUntilDate();
			if (time != null && time.isAfterOrEqual(new TimeTool())) {
				return "OK";
			}
			
			return "?";
		}
		
		private boolean isStopped(String endDate){
			if (endDate != null && endDate.length() > 4) {
				return true;
			}
			return false;
		}
	}
	
	/**
	 * 
	 * @param i
	 * @return the respective {@link ViewerSortOrder} for i, or {@link ViewerSortOrder#DEFAULT} if
	 *         invalid or not found
	 */
	public static ViewerSortOrder getSortOrderPerValue(int i){
		for (ViewerSortOrder cvso : ViewerSortOrder.values()) {
			if (cvso.val == i)
				return cvso;
		}
		return null;
	}
}
