package ch.elexis.core.ui.medication.views;

import java.util.List;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Color;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.data.Artikel;
import ch.elexis.data.Prescription;
import ch.elexis.data.Prescription.EntryType;
import ch.rgw.tools.TimeTool;

public class MedicationCellLabelProvider extends ColumnLabelProvider {
	
	private Color prnColor;
	private static final int FILTER_PRESCRIPTION_AFTER_N_DAYS = 30;
	private static MedicationComposite mediComposite;
	
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

	
	public static boolean isNotHistorical(Prescription presc){
		String[] dates = new String[2];
		presc.get(new String[] {
			Prescription.FLD_DATE_FROM, Prescription.FLD_DATE_UNTIL
		}, dates);
		
		if (presc.getEntryType() == EntryType.FIXED_MEDICATION) {
			return true;
		}
		
		TimeTool tt = new TimeTool(dates[0]);
		int daysTo = tt.daysTo(new TimeTool());
		if (daysTo > FILTER_PRESCRIPTION_AFTER_N_DAYS)
			return false;
		
		// stopped
		if (presc.getDosis().equals(StringConstants.ZERO))
			return false;
		
		String atcCode = presc.getArtikel().getATC_code();
		if (atcCode != null && atcCode.length() > 4) {
			// vaccinations start with atcCode J07
			if (atcCode.toUpperCase().startsWith("J07")) {
				return false;
			}
		}
		return true;
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
