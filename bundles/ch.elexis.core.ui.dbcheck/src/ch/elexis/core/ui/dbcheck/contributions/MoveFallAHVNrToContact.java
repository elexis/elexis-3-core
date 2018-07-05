package ch.elexis.core.ui.dbcheck.contributions;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.constants.XidConstants;
import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.data.Fall;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.data.Xid;

/**
 * As defined in bug 1439 - https://redmine.medelexis.ch/issues/1439 We have to move all definitions
 * of an AHV number allocated to a Fall to the respective contact.
 */
public class MoveFallAHVNrToContact extends ExternalMaintenance {
	
	public static final String AHV_NUMMER = "AHV-Nummer";
	
	public MoveFallAHVNrToContact(){}
	
	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion){
		StringBuilder output = new StringBuilder();
		
		Query<Fall> qbe = new Query<Fall>(Fall.class);
		List<Fall> qre = qbe.execute();
		pm.beginTask("Verschiebe Fall.AHV-Nummer Zuordnung zu Kontakt", qre.size());
		for (Fall fall : qre) {
			String ahvNummer = fall.getRequiredString(AHV_NUMMER);
			Patient pat = fall.getPatient();
			if (pat == null) {
				output.append("Patient für Fall " + fall.getLabel() + " nicht gefunden!");
				continue;
			}
			String patAhvNummer = pat.getXid(XidConstants.DOMAIN_AHV);
			
			if (patAhvNummer.length() < 1 && ahvNummer.length() > 1) {
				output
					.append("Setze AHV Nummer für " + pat.getLabel() + " auf " + ahvNummer + "\n");
				pat.addXid(XidConstants.DOMAIN_AHV, ahvNummer, true);
			}
			
			Map extinfo = fall.getMap(Fall.FLD_EXTINFO);
			if (extinfo.containsKey(AHV_NUMMER)) {
				extinfo.remove(AHV_NUMMER);
				fall.setMap(Fall.FLD_EXTINFO, extinfo);
				output.append("Entferne AHV-Nummer Eintrag aus Fall " + fall.getLabel() + "\n");
			}
			
			pm.worked(1);
		}
		pm.done();
		
		return output.toString();
	}
	
	@Override
	public String getMaintenanceDescription(){
		return "Fall.AHV-Nummer zu Kontakt.AHV-Nummer verschieben [1439]";
	}
	
}
