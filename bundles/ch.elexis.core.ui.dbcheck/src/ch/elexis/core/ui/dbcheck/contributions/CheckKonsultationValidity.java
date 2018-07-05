package ch.elexis.core.ui.dbcheck.contributions;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Query;
import ch.elexis.data.Verrechnet;

/**
 * check if each {@link Konsultation} has a valid {@link Fall} set and the {@link Verrechnet} items
 * are resolvable
 * 
 * @author Lucia
 *
 */
public class CheckKonsultationValidity extends ExternalMaintenance {
	
	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion){
		StringBuilder output = new StringBuilder();
		pm.beginTask("Check validity of konsultationen", 2);
		
		pm.subTask("Checking case references ...");
		Query<Konsultation> qbe = new Query<Konsultation>(Konsultation.class);
		List<Konsultation> kons = qbe.execute();
		output.append("Konsultationen mit leerem oder ungültigem Fall:\n");
		int counter = 0;
		for (Konsultation k : kons) {
			if (k.getFall() == null || !k.getFall().exists()) {
				output.append("Kons.ID: " + k.getLabel() + " (" + k.getId() + ") , Datum: "
					+ k.getDatum() + "\n");
				counter++;
			}
		}
		output = feedbackIfOK(output, counter);
		
		pm.worked(1);
		
		pm.subTask("Checking leistungs references ...");
		Query<Verrechnet> qbe1 = new Query<Verrechnet>(Verrechnet.class);
		List<Verrechnet> verrechnet = qbe1.execute();
		
		output.append("\nKonsultationen mit ungültigen Leistungen:\n");
		counter = 0;
		for (Verrechnet v : verrechnet) {
			IVerrechenbar verrechenbar = v.getVerrechenbar();
			try {
				// skip konsultationen with no leistungen
				if (verrechenbar != null) {
					verrechenbar.getMinutes();
				}
			} catch (NullPointerException npe) {
				if (v.getKons() != null) {
					output.append("Kons.ID: " + v.getKons().getLabel() + " (" + v.getKons().getId()
						+ "), Patient: " + v.getKons().getFall().getPatient().getLabel()
						+ ", LeistungsCode: " + v.get(Verrechnet.LEISTG_CODE) + ", Klasse: "
						+ v.get(Verrechnet.CLASS) + "\n");
					counter++;
				}
			}
		}
		output = feedbackIfOK(output, counter);
		pm.worked(1);
		
		output.append("\nPrüfung abgeschlossen.");
		pm.done();
		return output.toString();
	}
	
	private StringBuilder feedbackIfOK(StringBuilder output, int counter){
		if (counter == 0) {
			output.append("Keine ungültigen gefunden!");
		}
		return output;
	}
	
	@Override
	public String getMaintenanceDescription(){
		return "Konsultationen auf Gültigkeit überprüfen";
	}
	
}
