package ch.elexis.core.ui.dbcheck.contributions;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.data.Query;
import ch.elexis.data.Verrechnet;
import ch.elexis.data.VerrechnetCopy;

public class RemoveVatFromPandemieVerrechnet extends ExternalMaintenance {
	
	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion){
		StringBuilder sb = new StringBuilder();
		int updateCnt = 0;
		Query<Verrechnet> query = new Query<>(Verrechnet.class);
		query.add(Verrechnet.CLASS, Query.EQUALS, "ch.elexis.data.PandemieLeistung");
		List<Verrechnet> existing = query.execute();
		for (Verrechnet verrechnet : existing) {
			String value = (String) verrechnet.getDetail(Verrechnet.VATSCALE);
			if (value != null && !value.isEmpty() && !"0.0".equals(value)) {
				verrechnet.setDetail(Verrechnet.VATSCALE, Double.toString(0.0));
				updateCnt++;
			}
		}
		sb.append("Es wurden " + updateCnt + " verrechnete Pandemie Leistungen von "
			+ existing.size() + " MWST 0 gesetzt");
		updateCnt = 0;
		Query<VerrechnetCopy> queryCopy = new Query<>(VerrechnetCopy.class);
		queryCopy.add(VerrechnetCopy.CLASS, Query.EQUALS, "ch.elexis.data.PandemieLeistung");
		List<VerrechnetCopy> existingCopy = queryCopy.execute();
		for (VerrechnetCopy verrechnetCopy : existingCopy) {
			String value = (String) verrechnetCopy.getDetail(Verrechnet.VATSCALE);
			if (value != null && !value.isEmpty() && !"0.0".equals(value)) {
				verrechnetCopy.setDetail(Verrechnet.VATSCALE,
					Double.toString(0.0));
				updateCnt++;
			}
		}
		sb.append("\nEs wurden " + updateCnt + " stornierte Pandemie Leistungen von "
			+ existingCopy.size() + " MWST 0 gesetzt");
		return sb.toString();
	}
	
	@Override
	public String getMaintenanceDescription(){
		return "MWST aller verrechneten Pandemie Leistungen 0 setzen";
	}
}
