package ch.elexis.core.ui.dbcheck.contributions;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.data.Query;
import ch.elexis.data.Verrechnet;
import ch.elexis.data.VerrechnetCopy;

public class RemoveVatFromVerrechnet extends ExternalMaintenance {
	
	private String tarifTyp;
	
	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion){
		StringBuilder sb = new StringBuilder();
		tarifTyp = null;
		Display.getDefault().syncExec(() -> {
			MessageDialog dialog =
				new MessageDialog(Display.getDefault().getActiveShell(), "MWST 0 setzen", null,
					"Für welchen verrechneten Leistungs-Typ soll die MWST 0 gesetzt werden?",
					MessageDialog.QUESTION, 0, "Pandemie", "Tarmed");
			int selection = dialog.open();
			tarifTyp = getTarifTyp(selection);
		});
		
		if (StringUtils.isNotBlank(tarifTyp)) {
			sb.append("Ausgewählter Leistungs-Typ [" + tarifTyp + "]\n\n");
			int updateCnt = 0;
			Query<Verrechnet> query = new Query<>(Verrechnet.class);
			query.add(Verrechnet.CLASS, Query.EQUALS, tarifTyp);
			List<Verrechnet> existing = query.execute();
			pm.beginTask("MWST 0 bei Leistungen", existing.size());
			for (Verrechnet verrechnet : existing) {
				String value = (String) verrechnet.getDetail(Verrechnet.VATSCALE);
				if (value != null && !value.isEmpty() && !"0.0".equals(value)) {
					verrechnet.setDetail(Verrechnet.VATSCALE, Double.toString(0.0));
					updateCnt++;
				}
				pm.worked(1);
			}
			sb.append("Es wurden " + updateCnt + " verrechnete Leistungen von "
				+ existing.size() + " MWST 0 gesetzt");
			updateCnt = 0;
			Query<VerrechnetCopy> queryCopy = new Query<>(VerrechnetCopy.class);
			queryCopy.add(VerrechnetCopy.CLASS, Query.EQUALS, tarifTyp);
			List<VerrechnetCopy> existingCopy = queryCopy.execute();
			pm.beginTask("MWST 0 bei Kopien der Leistungen", existing.size());
			for (VerrechnetCopy verrechnetCopy : existingCopy) {
				String value = (String) verrechnetCopy.getDetail(Verrechnet.VATSCALE);
				if (value != null && !value.isEmpty() && !"0.0".equals(value)) {
					verrechnetCopy.setDetail(Verrechnet.VATSCALE, Double.toString(0.0));
					updateCnt++;
				}
				pm.worked(1);
			}
			sb.append("\nEs wurden " + updateCnt + " Kopien der Leistungen von "
				+ existingCopy.size() + " MWST 0 gesetzt");
			
		} else {
			sb.append("Unbekannter Tarif Typ");
		}
		return sb.toString();
	}
	
	private String getTarifTyp(int selection){
		if (selection == 0) {
			return "ch.elexis.data.PandemieLeistung";
		} else if (selection == 1) {
			return "ch.elexis.data.TarmedLeistung";
		}
		return null;
	}
	
	@Override
	public String getMaintenanceDescription(){
		return "MWST aller verrechneten Leistungen eines Typs 0 setzen";
	}
}
