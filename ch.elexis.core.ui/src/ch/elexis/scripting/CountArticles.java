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
import java.util.Map.Entry;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.data.Artikel;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Query;
import ch.elexis.data.Rechnung;
import ch.elexis.data.Verrechnet;
import ch.elexis.data.Zahlung;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;

public class CountArticles {
	HashMap<IVerrechenbar, Integer> paid = new HashMap<IVerrechenbar, Integer>();
	HashMap<IVerrechenbar, Integer> unpaid = new HashMap<IVerrechenbar, Integer>();
	Money mPaid = new Money();
	Money mUnpaid = new Money();
	
	/**
	 * Check all sold Articles between fromDate and untilDate (inclusive) and count paid and unpaid
	 * as per reference date
	 * 
	 * @param fromDate
	 *            Date to begin counting (inclusive)
	 * @param untilDate
	 *            date up to counting (inclusive)
	 * @param referenceDate
	 *            date relevant for paid status
	 * @param outputFile
	 *            path to file to write detail data into or null (don't write)
	 * @return A String describing sums calculated
	 */
	public String run(String fromDate, String untilDate, String referenceDate, String outputFile){
		try {
			Query<Konsultation> qbe = new Query<Konsultation>(Konsultation.class);
			qbe.add(Konsultation.FLD_DATE, Query.GREATER_OR_EQUAL,
				new TimeTool(fromDate).toString(TimeTool.DATE_COMPACT));
			qbe.add(Konsultation.DATE, Query.LESS_OR_EQUAL,
				new TimeTool(untilDate).toString(TimeTool.DATE_COMPACT));
			qbe.add(Konsultation.FLD_MANDATOR_ID, Query.EQUALS, CoreHub.actMandant.getId());
			TimeTool refDate = new TimeTool(referenceDate);
			for (Konsultation k : qbe.execute()) {
				boolean bPaid = false;
				Rechnung r = k.getRechnung();
				if (r != null) {
					if (r.getOffenerBetrag().isNeglectable()) {
						List<Zahlung> payments = r.getZahlungen();
						for (Zahlung z : payments) {
							if (new TimeTool(z.getDatum()).isBeforeOrEqual(refDate)) {
								bPaid = true;
								break;
							}
						}
					}
				}
				List<Verrechnet> vv = k.getLeistungen();
				for (Verrechnet v : vv) {
					IVerrechenbar iv = v.getVerrechenbar();
					if (iv instanceof Artikel) {
						Money price = v.getNettoPreis();
						int count = v.getZahl();
						price.multiply(count);
						if (bPaid) {
							mPaid.addMoney(price);
							Integer sum = paid.get(iv);
							if (sum == null) {
								sum = new Integer(count);
								paid.put(iv, sum);
							} else {
								sum += count;
							}
						} else {
							mUnpaid.addMoney(price);
							Integer sum = unpaid.get(iv);
							if (sum == null) {
								sum = new Integer(count);
								unpaid.put(iv, sum);
							} else {
								sum += count;
							}
						}
						
					}
				}
			}
			if (outputFile != null) {
				File file = new File(outputFile);
				FileWriter fw = new FileWriter(file);
				CSVWriter cs = new CSVWriter(fw);
				cs.writeNext(new String[] {
					"Artikelstatistik per", refDate.toString(TimeTool.DATE_GER)
				});
				cs.writeNext(new String[] {
					"Bezahlte Artikel", "Anzahl"
				});
				for (Entry<IVerrechenbar, Integer> entry : paid.entrySet()) {
					IVerrechenbar iv = entry.getKey();
					Integer sum = entry.getValue();
					cs.writeNext(new String[] {
						iv.getText(), sum.toString()
					});
				}
				cs.writeNext(new String[] {
					"Gesamtbetrag:", mPaid.getAmountAsString()
				});
				cs.writeNext(new String[] {});
				cs.writeNext(new String[] {
					"Unbezahlte Artikel", "Anzahl"
				});
				for (Entry<IVerrechenbar, Integer> entry : unpaid.entrySet()) {
					IVerrechenbar iv = entry.getKey();
					Integer sum = entry.getValue();
					cs.writeNext(new String[] {
						iv.getText(), sum.toString()
					});
				}
				cs.writeNext(new String[] {
					"Gesamtbetrag:", mUnpaid.getAmountAsString()
				});
				cs.close();
			}
			StringBuilder sb = new StringBuilder();
			sb.append("Artikelabgabe von (einschliesslich) ")
				.append(new TimeTool(fromDate).toString(TimeTool.DATE_GER))
				.append(" bis (einschliesslich) ")
				.append(new TimeTool(untilDate).toString(TimeTool.DATE_GER))
				.append(":\n")
				.append("Bezahlte Artikel: ")
				.append(mPaid.getAmountAsString())
				.append("\n")
				.append("Unbezahlte Artikel: ")
				.append(mUnpaid.getAmountAsString())
				.append("\n")
				.append(
					"(Jeweils per Stichtag "
						+ new TimeTool(referenceDate).toString(TimeTool.DATE_GER)).append(")\n");
			return sb.toString();
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return "Error executing Script: " + ex.getMessage();
		}
	}
}
