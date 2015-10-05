package ch.elexis.core.ui.medication.views;

import java.util.List;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Color;

import ch.elexis.core.ui.UiDesk;
import ch.elexis.data.Artikel;
import ch.elexis.data.Prescription;
import ch.rgw.tools.TimeTool;

public class MedicationCellLabelProvider extends ColumnLabelProvider {
	
	private Color reserveColor;
	
	public MedicationCellLabelProvider(){
		reserveColor = UiDesk.getColorFromRGB("DDEFFF");
	}

	@Override
	public Color getBackground(Object element){
		Prescription pres = (Prescription) element;
		if (pres.isReserveMedication()) {
			return reserveColor;
		}
		
		return null;
	}
	
	@Override
	public Color getForeground(Object element){
		Prescription pres = (Prescription) element;
		if (!pres.isFixedMediation() && !hasDateUntil(pres))
			return UiDesk.getColor(UiDesk.COL_RED);
			
		return super.getForeground(element);
	}

	
	private static boolean hasDateUntil(Prescription presc){
		String date = presc.get(Prescription.FLD_DATE_UNTIL);
		if (date.length() == 0) {
			return false;
		}
		return true;
	}
	
	public static boolean isNoTwin(Prescription presc, List<Prescription> prescriptions){
		if (presc.isFixedMediation())
			return true;
		
		Artikel arti = presc.getArtikel();
		TimeTool start = new TimeTool(presc.getBeginDate());
		TimeTool tt = new TimeTool();
		long lastUpdate = presc.getLastUpdate();
		
		for (Prescription p : prescriptions) {
			if (!(p.getId().equals(presc.getId()))) {
				if (p.getArtikel().equals(arti)) {
					if (p.isFixedMediation()) {
						return false;
					} else {
						tt.set(p.getBeginDate());
						if (tt.isAfter(start)) {
							return false;
						} else if (tt.isEqual(start)) {
							tt.setTimeInMillis(p.getLastUpdate());
							TimeTool updateTime = new TimeTool(lastUpdate);
							if (tt.isAfter(updateTime)) {
								return false;
							}
						}
					}
				}
			}
		}
		return true;
	}
}
