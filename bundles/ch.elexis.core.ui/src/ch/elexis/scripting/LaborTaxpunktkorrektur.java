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
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.data.Verrechnet;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.Money;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class LaborTaxpunktkorrektur {
	FileWriter writer;
	
	/**
	 * Rechne alle Konsultationen seit einem Stichdatum (inklusive) auf einen neuen Taxpunktwert um
	 * 
	 * @param dateFrom
	 * @param newTP
	 * @return Einen String, der das Resulktat beschreibt.
	 */
	public String recalc(String dateFrom, double newTP){
		File file =
			new File(System.getProperty("user.home") + File.separator + "elexis" + File.separator
				+ "taxpunktkorrektur.log");
		try {
			writer = new FileWriter(file);
			if (SWTHelper.askYesNo("WARNUNG", "Wirklich alle Konsultationen seit dem " + dateFrom
				+ " auf " + Double.toString(newTP) + " umrechnen?")) {
				TimeTool ttFrom = new TimeTool();
				if (!ttFrom.set(dateFrom)) {
					writer.write("bad date format: " + dateFrom + " aborting.\n");
					return "Datumformat kann nicht interpretiert werden. Bitte als dd.mm.yyyy eingeben";
				}
				StringBuilder del = new StringBuilder();
				del.append("DELETE FROM VK_PREISE WHERE TYP='EAL'").append(" and DATUM_VON>=")
					.append(JdbcLink.wrap(ttFrom.toString(TimeTool.DATE_COMPACT)));
				writer.write("removing old tp values\n");
				PersistentObject.getConnection().exec(del.toString());
				del = new StringBuilder();
				TimeTool yesterday = new TimeTool(ttFrom);
				yesterday.addDays(-1);
				del.append("UPDATE VK_PREISE SET DATUM_BIS=")
					.append(JdbcLink.wrap(yesterday.toString(TimeTool.DATE_COMPACT)))
					.append(" WHERE TYP='EAL' AND DATUM_BIS>")
					.append(JdbcLink.wrap(yesterday.toString(TimeTool.DATE_COMPACT)));
				writer.write("adjusting multiplicators\n");
				PersistentObject.getConnection().exec(del.toString());
				del = new StringBuilder();
				del.append("INSERT INTO VK_PREISE (ID,DATUM_VON,DATUM_BIS,TYP,MULTIPLIKATOR) VALUES (")
					.append(JdbcLink.wrap(StringTool.unique("rtsu"))).append(",")
					.append(JdbcLink.wrap(ttFrom.toString(TimeTool.DATE_COMPACT))).append(",")
					.append("'99991231','EAL',").append(JdbcLink.wrap(Double.toString(newTP)))
					.append(");");
				writer.write("inserting new TP value\n");
				PersistentObject.getConnection().exec(del.toString());
				writer.write("collecting consultations\n");
				Query<Konsultation> qbe = new Query<Konsultation>(Konsultation.class);
				qbe.add("RechnungsID", "is", null);
				qbe.add("Datum", ">=", ttFrom.toString(TimeTool.DATE_COMPACT));
				int i = 0;
				Money old = new Money();
				Money changed = new Money();
				for (Konsultation k : qbe.execute()) {
					List<Verrechnet> vv = k.getLeistungen();
					for (Verrechnet v : vv) {
						if (v.getVerrechenbar().getCodeSystemName().equals("Laborleistung")) {
							old.addMoney(new Money(v.get("VK_Preis")).multiply(v.getZahl() / 100.0));
							v.setStandardPreis();
							changed.addMoney(v.getBruttoPreis().multiply(v.getZahl()));
						}
					}
					writer.write("konvertierte " + k.getVerboseLabel() + "\n");
					i++;
				}
				
				return "Abgeschlossen. " + Integer.toString(i)
					+ " Konsultationen wurden umgrechnet.\nAlter Gesamtbetrag: "
					+ old.getAmountAsString() + "\nNeuer Gesamtbetrag: "
					+ changed.getAmountAsString();
			} else {
				writer.write("aborted by user\n");
			}
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return ex.getMessage();
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {}
		}
		return "allgemeiner Fehler";
	}
}
