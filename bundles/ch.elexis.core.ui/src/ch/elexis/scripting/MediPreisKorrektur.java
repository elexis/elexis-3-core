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
import java.util.List;

import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Query;
import ch.elexis.data.Rechnung;
import ch.elexis.data.RnStatus;
import ch.elexis.data.Verrechnet;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;

public class MediPreisKorrektur {
	FileWriter writer;
	
	public String recalc(String dateFrom){
		File file =
			new File(System.getProperty("user.home") + File.separator + "elexis" + File.separator
				+ "medipreiskorrektur.log");
		try {
			writer = new FileWriter(file);
			if (SWTHelper.askYesNo("WARNUNG",
				"Wirklich alle Konsultationen seit (einschliesslich) dem " + dateFrom
					+ " auf den aktuell gültigen Medikamentenpreis umrechnen?")) {
				TimeTool ttFrom = new TimeTool();
				if (!ttFrom.set(dateFrom)) {
					writer.write("bad date format: " + dateFrom + " aborting.\n");
					return "Datumformat kann nicht interpretiert werden. Bitte als dd.mm.yyyy eingeben";
				}
				Query<Konsultation> qbe = new Query<Konsultation>(Konsultation.class);
				qbe.add(Konsultation.DATE, ">=", ttFrom.toString(TimeTool.DATE_COMPACT));
				int i = 0;
				Money old = new Money();
				Money changed = new Money();
				for (Konsultation kons : qbe.execute()) {
					writer.write("\nKonsultation: " + kons.getLabel());
					Rechnung rn = kons.getRechnung();
					if ((rn != null) && (rn.getStatus() != RnStatus.STORNIERT)) {
						writer.write(": Rechnung bereits erstellt, übersprungen.");
					} else {
						i++;
						List<Verrechnet> vv = kons.getLeistungen();
						for (Verrechnet v : vv) {
							String codesystem = v.getVerrechenbar().getCodeSystemCode();
							if (codesystem.startsWith("Medi")) {
								old.addMoney(new Money(v.get("VK_Preis")).multiply(v.getZahl() / 100.0));
								v.setStandardPreis();
								changed.addMoney(v.getBruttoPreis().multiply(v.getZahl()));
							}
						}
						writer.write("konvertiert. ");
					}
				}
				StringBuilder sb = new StringBuilder();
				sb.append("Konversion beendet. ").append(i)
					.append(" Konsultationen wurden umgerechnet\n").append("Alter Betrag: ")
					.append(old.getAmountAsString()).append("\n").append("Neuer Betrag: ")
					.append(changed.getAmountAsString()).append("\n");
				return sb.toString();
			} else {
				return "\nabgebrochen.";
			}
			
		} catch (Exception ex) {
			return "Fehler beim Ablauf.";
		}
	}
}
