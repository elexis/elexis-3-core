/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     G. Weirich - initial API and implementation
 ******************************************************************************/
package ch.elexis.scripting;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;

import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Mandant;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.data.Verrechnet;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.TimeTool;

public class LeistungenExport {
	class patums {
		String ID;
		TimeTool gebDat;
		String sex;
		TimeTool erstkons;
		TimeTool letztkons;
		Integer anzahlKons;
		Double costPerCons;
		Double costTotal;
		Double costTarmedAL;
		Double costTarmedTL;
		Double costMedicament;
		Double costMedical;
		Double costPhysio;
		Double costOther;
	}
	
	private HashMap<String, patums> patienten = new HashMap<String, LeistungenExport.patums>();
	
	public String doExport(String from, String until, String toDir){
		TimeTool ttFrom = new TimeTool();
		TimeTool ttUntil = new TimeTool();
		if (!ttFrom.set(from)) {
			return "Konnte Beginndatum nicht lesen: " + from;
		}
		if (!ttUntil.set(until)) {
			return "Konnte Enddatum nicht lesen: " + until;
		}
		try {
			int UUID = 0;
			int PatientID = 1;
			int FallID = 2;
			int KonsID = 3;
			int Datum = 4;
			int Mandant = 5;
			int CodeSystemName = 6;
			int CodeSystemCode = 7;
			int Code = 8;
			int Text = 9;
			int Kostentraeger = 10;
			int TarmedAL = 11;
			int TarmedTL = 12;
			int Physio = 13;
			int Labor = 14;
			int Medikament = 15;
			int Medical = 16;
			int Migel = 17;
			int kantonal = 18;
			int andere = 19;
			String[] cols =
				new String[] {
					"UUID", "PatientID", "FallID", "KonsID", "Datum", "Mandant", "CodesystemName",
					"CodesystemCode", "Code", "Text", "Kostentraeger", "TarmedAL", "TarmedTL",
					"Physio", "Labor", "Medikament", "Medical", "MiGEL", "Kantonal", "Andere"
				};
			File dir = new File(toDir);
			if (!dir.exists() || (!dir.isDirectory())) {
				return dir + " nicht gefunden oder ist kein Verzeichnis.";
			}
			File outFile = new File(dir, "Verrechnungen.csv");
			FileWriter writer = new FileWriter(outFile);
			CSVWriter out = new CSVWriter(writer);
			out.writeNext(cols);
			Query<Konsultation> qbe = new Query<Konsultation>(Konsultation.class);
			qbe.add("Datum", ">=", ttFrom.toString(TimeTool.DATE_COMPACT));
			qbe.add("Datum", "<=", ttUntil.toString(TimeTool.DATE_COMPACT));
			for (Konsultation k : qbe.execute()) {
				Fall fall = k.getFall();
				if (fall != null) {
					Patient pat = fall.getPatient();
					if (pat != null) {
						patums pu = patienten.get(pat.getId());
						if (pu == null) {
							pu = new patums();
							pu.ID = pat.getId();
							pu.anzahlKons = 0;
							pu.costMedical = 0.0;
							pu.costMedicament = 0.0;
							pu.costOther = 0.0;
							pu.costPerCons = 0.0;
							pu.costPhysio = 0.0;
							pu.costTarmedAL = 0.0;
							pu.costTarmedTL = 0.0;
							pu.costTotal = 0.0;
							patienten.put(pat.getId(), pu);
						}
						pu.anzahlKons += 1;
						String fallid = fall.getId();
						String Bezeichnung = fall.getBezeichnung();
						String gesetz = fall.getConfiguredBillingSystemLaw().name();
						String abr = fall.getAbrechnungsSystem();
						Kontakt kt = fall.getGarant();
						String rechnungsempfaenger = "";
						if (kt != null) {
							rechnungsempfaenger = fall.getGarant().getLabel();
						}
						Kontakt costBearer = fall.getCostBearer();
						String kostentraeger = "";
						String versnr = "";
						if (costBearer != null) {
							kostentraeger = costBearer.getLabel();
							versnr = fall.getRequiredString("Versicherungsnummer");
						}
						List<Verrechnet> vr = k.getLeistungen();
						Mandant m = k.getMandant();
						if (m != null) {
							for (Verrechnet v : vr) {
								String[] col = new String[cols.length];
								for (int i = 0; i < cols.length; i++) {
									col[i] = "";
								}
								col[UUID] = v.getId();
								col[PatientID] = pat.getId();
								col[FallID] = fall.getId();
								col[KonsID] = k.getId();
								col[Datum] = k.getDatum();
								col[Mandant] = m.getId();
								IVerrechenbar vv = v.getVerrechenbar();
								if (vv != null) {
									col[CodeSystemName] = vv.getCodeSystemName();
									col[CodeSystemCode] = vv.getCodeSystemCode();
									col[Code] = vv.getCode();
									col[Text] = vv.getText();
									int offset = andere;
									if (vv.getCodeSystemName().equals("Tarmed")) {
										offset = TarmedAL;
									} else if (vv.getCodeSystemName().equals("Laborleistung")) {
										offset = Labor;
									} else if (vv.getCodeSystemName().equals("Physiotherapie")) {
										offset = Physio;
									} else if (vv.getCodeSystemName().equals("Medicals")) {
										offset = Medical;
									} else if (vv.getCodeSystemName().startsWith("Medikament")) {
										offset = Medikament;
									} else if (vv.getCodeSystemName().equals("MiGeL")) {
										offset = Migel;
									}
									col[offset] = v.getNettoPreis().getAmountAsString();
								}
								out.writeNext(col);
							}
						}
					}
					
				} // fall!=null
				out.close();
			}
			return "Ok. Die gewünschten Dateien sind erstellt.";
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return "Exception: " + ex.getClass().getName() + "; " + ex.getMessage();
		}
	}
}
