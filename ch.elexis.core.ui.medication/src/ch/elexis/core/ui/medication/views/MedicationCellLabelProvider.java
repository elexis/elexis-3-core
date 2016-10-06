package ch.elexis.core.ui.medication.views;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Color;

import ch.elexis.core.ui.UiDesk;
import ch.elexis.data.Prescription;
import ch.elexis.data.Prescription.EntryType;

public class MedicationCellLabelProvider extends ColumnLabelProvider {
	
	private Color reserveColor;
	
	public MedicationCellLabelProvider(){
		reserveColor = UiDesk.getColorFromRGB("DDEFFF");
	}

	@Override
	public Color getBackground(Object element){
		MedicationTableViewerItem pres = (MedicationTableViewerItem) element;
		if (pres.isReserveMedication()) {
			return reserveColor;
		}
		
		return null;
	}
	
	@Override
	public Color getForeground(Object element){
		Prescription prescription = ((MedicationTableViewerItem) element).getPrescription();
		if (prescription != null) {
			if (prescription.getEntryType() != EntryType.SELF_DISPENSED
				&& prescription.getEntryType() != EntryType.RECIPE) {
				if (prescription.getEndDate() != null && !prescription.getEndDate().isEmpty()) {
					return UiDesk.getColor(UiDesk.COL_RED);
				}
			}
		}
		
		return super.getForeground(element);
	}
}
