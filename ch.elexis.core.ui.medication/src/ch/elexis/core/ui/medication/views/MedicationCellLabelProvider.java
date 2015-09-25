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
	
	private Color reserveColor;
	private static final int FILTER_PRESCRIPTION_AFTER_N_DAYS = 30;
	
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
		if (!isNotHistorical((Prescription) element)) {
			return UiDesk.getColor(UiDesk.COL_DARKGREY);
		}
		if (!pres.isFixedMediation() && !hasDateUntil(pres))
			return UiDesk.getColor(UiDesk.COL_RED);
		return super.getForeground(element);
	}

	
	/**
	 * Medication is not Historical if it is... <br>
	 * <ul style="list-style-type:disc">
	 * <li>a FixMedication</li>
	 * <ul>
	 * <li>EXPECT stopped FixMedication</li>
	 * </ul>
	 * <li>from the past 30 days</li>
	 * <ul>
	 * <li>applied</li>
	 * <li>self dispensed</li>
	 * <li>recipe</li>
	 * <li>EXPECT vaccinations (ATC codes starting with 'J07')</li>
	 * </ul>
	 * <li>a ReserveMedication</li>
	 * </ul>
	 * 
	 * @param presc
	 * @return {@link <code>true</code>} if to be displayed, {@link <code>false</code>} if
	 *         historical
	 */
	public static boolean isNotHistorical(Prescription presc){
		// get start and end date
		String[] dates = new String[2];
		presc.get(new String[] {
			Prescription.FLD_DATE_FROM, Prescription.FLD_DATE_UNTIL
		}, dates);
		
		// is it a active FixMedication
		EntryType type = presc.getEntryType();
		if (type == EntryType.FIXED_MEDICATION) { //check if backward compatible
			// stopped?
			if (StringConstants.ZERO.equals(presc.getDosis())) {
				return false;
			}
			return true;
		}
		
		// is a ReserveMedication
		if (type == EntryType.RESERVE_MEDICATION) {
			return true;
		}
		
		// medicine from the past 30 days
		TimeTool time = new TimeTool(dates[0]);
		int daysTo = time.daysTo(new TimeTool());
		if (daysTo > FILTER_PRESCRIPTION_AFTER_N_DAYS) {
			return false;
		}
		
		// is no vaccination (atc starting with 'J07')
		if (presc.getArtikel() != null) {
			String atcCode = presc.getArtikel().getATC_code();
			if (atcCode != null && atcCode.length() > 4
				&& atcCode.toUpperCase().startsWith("J07")) {
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
