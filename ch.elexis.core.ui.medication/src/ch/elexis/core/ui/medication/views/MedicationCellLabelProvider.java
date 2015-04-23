package ch.elexis.core.ui.medication.views;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Color;

import ch.elexis.core.ui.UiDesk;
import ch.elexis.data.Prescription;
import ch.rgw.tools.TimeTool;

public class MedicationCellLabelProvider extends ColumnLabelProvider {
	
	private Color prnColor;
	private static final int FILTER_PRESCRIPTION_AFTER_N_DAYS = 30;
	
	public MedicationCellLabelProvider(){
		prnColor = UiDesk.getColorFromRGB("66CDAA");
	}

	@Override
	public Color getBackground(Object element){
		Prescription pres = (Prescription) element;
		if(pres.getReserveMedication()) return prnColor;
		
		return null;
	}
	
	@Override
	public Color getForeground(Object element){
		Prescription pres = (Prescription) element;
		if (!isNotHistorical((Prescription) element)) {
			return UiDesk.getColor(UiDesk.COL_DARKGREY);
		}
		if (!pres.isFixedMediation() && !hasDateUntil(pres))
			return UiDesk.getColor(UiDesk.COL_RED);
		return super.getForeground(element);
	}

	
	public static boolean isNotHistorical(Prescription element) {
		Prescription presc = (Prescription) element;
		String[] dates = new String[2];
		presc.get(new String[] {
			Prescription.FLD_DATE_FROM, Prescription.FLD_DATE_UNTIL
		}, dates);
		
		TimeTool tt = new TimeTool(dates[0]);
		int daysTo = tt.daysTo(new TimeTool());
		if (daysTo > FILTER_PRESCRIPTION_AFTER_N_DAYS)
			return false;
		return true;
	}
	
	private boolean hasDateUntil(Prescription presc){
		String date = presc.get(Prescription.FLD_DATE_UNTIL);
		if (date.length() == 0) {
			return false;
		}
		return true;
	}
}
