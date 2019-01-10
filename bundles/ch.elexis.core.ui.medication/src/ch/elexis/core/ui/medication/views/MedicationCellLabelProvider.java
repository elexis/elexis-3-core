package ch.elexis.core.ui.medication.views;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Color;

import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.ui.UiDesk;

public class MedicationCellLabelProvider extends ColumnLabelProvider {
	
	private Color reserveColor;
	
	public MedicationCellLabelProvider(){
		reserveColor = UiDesk.getColorFromRGB("DDEFFF");
	}

	@Override
	public Color getBackground(Object element){
		MedicationTableViewerItem pres = (MedicationTableViewerItem) element;
		if (pres.getEntryType() == EntryType.RESERVE_MEDICATION) {
			return reserveColor;
		}
		
		return null;
	}
	
	@Override
	public Color getForeground(Object element){
		IPrescription prescription = ((MedicationTableViewerItem) element).getPrescription();
		if (prescription != null) {
			if (prescription.getEntryType() != EntryType.SELF_DISPENSED
				&& prescription.getEntryType() != EntryType.RECIPE) {
				if (prescription.getDateTo() != null) {
					return UiDesk.getColor(UiDesk.COL_RED);
				}
			}
		}
			
		return super.getForeground(element);
	}
}
