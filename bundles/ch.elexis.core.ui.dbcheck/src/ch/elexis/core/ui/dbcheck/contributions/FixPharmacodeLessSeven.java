/*******************************************************************************
 * Copyright (c) 2011, Medevit OG and Medelexis AG
 * All rights reserved.
 *******************************************************************************/

package ch.elexis.core.ui.dbcheck.contributions;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.data.Artikel;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;

/**
 * Ticket #13: Repariert Einträge für Artikel deren Pharmacode kürzer als 7 Stellen ist
 * 
 * @author Marco Descher
 * 
 */
public class FixPharmacodeLessSeven extends ExternalMaintenance {
	
	public static final String PHARMACODE_EXTINFO_ID = "Pharmacode";
	
	public FixPharmacodeLessSeven(){}
	
	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion){
		StringBuilder output = new StringBuilder();
		pm.beginTask("Überprüfe Pharmacode Länge", 2);
		pm.subTask("Lese Artikel ein...");
		Query<Artikel> qbe = new Query<Artikel>(Artikel.class);
		qbe.or();
		qbe.add(PersistentObject.FLD_DELETED, Query.EQUALS, StringConstants.ONE);
		List<Artikel> qre = qbe.execute();
		pm.worked(1);
		pm.subTask("Korrigiere Einträge...");
		for (Iterator<Artikel> iterator = qre.iterator(); iterator.hasNext();) {
			Artikel artikel = (Artikel) iterator.next();
			
			String subId = artikel.get(Artikel.FLD_SUB_ID);
			Map<Object, Object> articleExtInfo = artikel.getMap(Artikel.FLD_EXTINFO);
			String subIdExtInfo = (String) articleExtInfo.get(PHARMACODE_EXTINFO_ID);
			
			int subIdLength = subId.length();
			if (subIdLength >= 7 && subIdExtInfo == null) {
				continue;
			} else if (subIdLength >= 7 && subIdExtInfo!=null && subIdExtInfo.length() >= 7) {
				continue;
			} else if (subIdLength == 0) {
				artikel.set(Artikel.FLD_SUB_ID, "0000000");
				output.append("Korrigiere " + artikel.getName()
					+ " von keine subId vorhanden auf 0000000\n");
				articleExtInfo.put(PHARMACODE_EXTINFO_ID, "0000000");
				artikel.setMap(Artikel.FLD_EXTINFO, articleExtInfo);
			} else {
				try {
					@SuppressWarnings("unused")
					int pharmaCode = Integer.parseInt(subId);
					StringBuilder sb = new StringBuilder();
					int missingZeros = 7 - subIdLength;
					while (missingZeros != 0) {
						sb.append("0");
						missingZeros--;
					}
					sb.append(subId);
					output.append("Korrigiere " + artikel.getName() + " von " + subId + " auf "
						+ sb.toString() + "\n");
					artikel.set(Artikel.FLD_SUB_ID, sb.toString());
					articleExtInfo.put(PHARMACODE_EXTINFO_ID, sb.toString());
					artikel.setMap(Artikel.FLD_EXTINFO, articleExtInfo);
				} catch (NumberFormatException e) {
					// We do not have a numeric value here, so it cant't be
					// a pharmacode or it is invalid for repair
					continue;
				}
			}
			
		}
		output.append(qre.size() + " Artikel überprüft.\n");
		
		pm.worked(1);
		pm.done();
		
		return output.toString();
	}
	
	@Override
	public String getMaintenanceDescription(){
		return "Artikel (auch gelöschte) mit Pharmacode <7 Zeichen reparieren";
	}
	
}
