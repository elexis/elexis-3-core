package ch.elexis.core.ui.medication.views;

import java.util.List;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Color;

import ch.elexis.core.ui.UiDesk;

import ch.rgw.tools.TimeTool;

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
		MedicationTableViewerItem pres = (MedicationTableViewerItem) element;
		if (!pres.isFixedMediation() && !(pres.getEndDate().length()==0))
			return UiDesk.getColor(UiDesk.COL_RED);
			
		return super.getForeground(element);
	}
	
	public static boolean isNoTwin(MedicationTableViewerItem presc, List<MedicationTableViewerItem> prescriptions){
		if (presc.isFixedMediation())
			return true;
		
		String arti = presc.getArtikelsts();
		TimeTool start = new TimeTool(presc.getBeginDate());
		TimeTool tt = new TimeTool();
		long lastUpdate = presc.getLastUpdate();
		
		for (MedicationTableViewerItem p : prescriptions) {
			if (!(p.getId().equals(presc.getId()))) {
				if (p.getArtikelsts()!=null && p.getArtikelsts().equals(arti)) {
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
