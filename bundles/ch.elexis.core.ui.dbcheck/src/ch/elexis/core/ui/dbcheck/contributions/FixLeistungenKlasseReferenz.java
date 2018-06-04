package ch.elexis.core.ui.dbcheck.contributions;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.data.Artikel;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.data.Verrechnet;

public class FixLeistungenKlasseReferenz extends ExternalMaintenance {
	
	public FixLeistungenKlasseReferenz(){}
	
	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion){
		StringBuilder output = new StringBuilder();
		pm.beginTask("Fixing class designations", 4);
		
		pm.subTask("Finding articles ...");
		
		Query<Artikel> qbe = new Query<Artikel>(Artikel.class);
		qbe.or();
		qbe.add(PersistentObject.FLD_DELETED, Query.EQUALS, StringConstants.ONE);
		List<Artikel> qre = qbe.execute();
		pm.worked(1);
		pm.subTask("Fixing entries ...");
		for (Iterator<Artikel> iterator = qre.iterator(); iterator.hasNext();) {
			Artikel artikel = (Artikel) iterator.next();
			String klasse = artikel.get(Artikel.FLD_KLASSE);
			int klasseLength = klasse.length();
			if (klasseLength > 0) {
				continue;
			} else {
				String artikelTyp = artikel.get(Artikel.FLD_TYP).trim();
				if (artikelTyp.equalsIgnoreCase("Medical")) {
					artikel.set(Artikel.FLD_KLASSE, "ch.elexis.artikel_ch.data.Medical");
					output.append("Fixing " + artikel.getName()
						+ " from no Klasse field entry to ch.elexis.artikel_ch.data.Medical\n");
				} else if (artikelTyp.equalsIgnoreCase("Medikament")) {
					artikel.set(Artikel.FLD_KLASSE, "ch.elexis.artikel_ch.data.Medikament");
					output.append("Setting " + artikel.getName()
						+ " from no Klasse field entry to ch.elexis.artikel_ch.data.Medikament\n");
				} else if (artikelTyp.equalsIgnoreCase("Medikamente")) {
					artikel.set(Artikel.FLD_KLASSE, "ch.elexis.artikel_ch.data.Medikament");
					output.append("Setting " + artikel.getName()
						+ " from no Klasse field entry to ch.elexis.artikel_ch.data.Medikament\n");
				} else if (artikelTyp.equalsIgnoreCase("Eigenartikel")) {
					artikel.set(Artikel.FLD_KLASSE, "ch.elexis.data.Eigenartikel");
					output.append("Setting " + artikel.getName()
						+ " from no Klasse field entry to ch.elexis.data.Eigenartikel\n");
				} else if (artikelTyp.equalsIgnoreCase("MiGeL")) {
					artikel.set(Artikel.FLD_KLASSE, "ch.elexis.artikel_ch.data.MiGelArtikel");
					output.append("Setting " + artikel.getName()
						+ " from no Klasse field entry to ch.elexis.artikel_ch.data.MiGelArtikel\n");
				}
			}
		}
		pm.worked(1);
		
		pm.subTask("Finding leistungen ...");
		Query<Verrechnet> qbeV = new Query<Verrechnet>(Verrechnet.class);
		qbeV.or();
		qbeV.add(PersistentObject.FLD_DELETED, Query.EQUALS, StringConstants.ONE);
		List<Verrechnet> qreV = qbeV.execute();
		pm.worked(1);
		pm.subTask("Fixing entries ...");
		for (Iterator<Verrechnet> iterator = qreV.iterator(); iterator.hasNext();) {
			Verrechnet ver = (Verrechnet) iterator.next();
			
			String leistungId = ver.get(Verrechnet.LEISTG_CODE);
			Artikel art = Artikel.load(leistungId);
			if (art.exists()) {
				ver.set(Verrechnet.CLASS, art.get(Artikel.FLD_KLASSE));
				output.append("Fixing Klasse entry for " + ver.getLabel() + " to "
					+ art.get(Artikel.FLD_KLASSE) + "\n");
			}
		}
		pm.worked(1);
		pm.done();
		
		return output.toString();
	}
	
	@Override
	public String getMaintenanceDescription(){
		return "Fix class type designations in article and leistungen";
	}
	
}
