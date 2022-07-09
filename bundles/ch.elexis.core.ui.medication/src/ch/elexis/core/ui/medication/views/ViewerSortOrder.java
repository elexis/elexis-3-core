package ch.elexis.core.ui.medication.views;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.prescription.EntryType;
import ch.rgw.tools.TimeTool;

public enum ViewerSortOrder {
	MANUAL("manuell", 0, new ManualViewerComparator()), DEFAULT("standard", 1, new DefaultViewerComparator()); //$NON-NLS-1$ //$NON-NLS-2$

	final String label;
	final int val;
	final ViewerComparator vc;

	private static final int DESCENDING = 1;
	private static int direction = DESCENDING;
	private static int propertyIdx = 0;
	private static boolean atcSort = false;
	private static TimeTool time1 = new TimeTool();
	private static TimeTool time2 = new TimeTool();

	private ViewerSortOrder(String label, int val, ViewerComparator vc) {
		this.label = label;
		this.val = val;
		this.vc = vc;
	}

	public int getDirection() {
		return direction == 1 ? SWT.DOWN : SWT.UP;
	}

	public void setAtcSort(boolean value) {
		atcSort = value;
	}

	public void setColumn(int column) {
		if (column == propertyIdx) {
			// Same column as last sort; toggle the direction
			direction = 1 - direction;
		} else {
			// New column; do an ascending sort
			propertyIdx = column;
			direction = DESCENDING;
		}
	}

	/**
	 * sort the medication order by manual ordering as stored in
	 * {@link IPrescription#FLD_SORT_ORDER}
	 */
	public static class ManualViewerComparator extends ViewerComparator {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			MedicationTableViewerItem p1 = (MedicationTableViewerItem) e1;
			MedicationTableViewerItem p2 = (MedicationTableViewerItem) e2;

			return Integer.compare(p1.getOrder(), p2.getOrder());
		}
	}

	/**
	 * sort the medication table viewer first by group (fixed medication or pro re
	 * nata medication), and then by natural article name
	 */
	public static class DefaultViewerComparator extends ViewerComparator {

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			MedicationTableViewerItem p1 = (MedicationTableViewerItem) e1;
			MedicationTableViewerItem p2 = (MedicationTableViewerItem) e2;
			// ignore colums and sort by atc
			if (atcSort) {
				String atc1 = p1.getAtc();
				String atc2 = p2.getAtc();
				if (atc1 == null) {
					atc1 = StringUtils.EMPTY;
				}
				if (atc2 == null) {
					atc2 = StringUtils.EMPTY;
				}
				return atc2.compareTo(atc1);
			}
			// sort by column
			int rc = 0;
			switch (propertyIdx) {
			case 0:
				EntryType et1 = p1.getEntryType();
				EntryType et2 = p2.getEntryType();
				rc = Integer.compare(et1.numericValue(), et2.numericValue());
				// sort article label if type is equal
				if (rc != 0) {
					break;
				}
			case 1:
				p1.resolve();
				p2.resolve();
				String l1 = p1.getArtikelLabel();
				String l2 = p2.getArtikelLabel();
				if (l1 == null) {
					l1 = StringUtils.EMPTY;
				}
				if (l2 == null) {
					l2 = StringUtils.EMPTY;
				}
				rc = l2.compareTo(l1);
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
				String com1 = p1.getRemark();
				String com2 = p2.getRemark();
				rc = com1.compareTo(com2);
				break;
			case 5:
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
				String stopReason1 = p1.getStopReason();
				if (stopReason1 == null)
					stopReason1 = StringUtils.EMPTY;

				String stopReason2 = p2.getStopReason();
				if (stopReason2 == null)
					stopReason2 = StringUtils.EMPTY;

				rc = stopReason1.compareTo(stopReason2);
				break;
			case 7:
				String prescriptor1 = p1.getPrescriptorLabel();
				if (prescriptor1 == null)
					prescriptor1 = StringUtils.EMPTY;

				String prescriptor2 = p2.getPrescriptorLabel();
				if (prescriptor2 == null)
					prescriptor2 = StringUtils.EMPTY;

				rc = prescriptor1.compareTo(prescriptor2);
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

		private boolean isStopped(String endDate) {
			if (endDate != null && endDate.length() > 4) {
				return true;
			}
			return false;
		}
	}

	/**
	 *
	 * @param i
	 * @return the respective {@link ViewerSortOrder} for i, or
	 *         {@link ViewerSortOrder#DEFAULT} if invalid or not found
	 */
	public static ViewerSortOrder getSortOrderPerValue(int i) {
		for (ViewerSortOrder cvso : ViewerSortOrder.values()) {
			if (cvso.val == i)
				return cvso;
		}
		return null;
	}
}
