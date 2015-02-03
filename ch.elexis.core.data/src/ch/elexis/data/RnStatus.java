/*******************************************************************************
 * Copyright (c) 2005-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.data;

public class RnStatus {
	public static final int UNBEKANNT = 0;
	public static final int VERRECHNET = 1;
	public static final int NICHT_VERRECHNET = 2;
	public static final int LAUFEND = 3;
	public static final int OFFEN = 4;
	public static final int OFFEN_UND_GEDRUCKT = 5;
	public static final int MAHNUNG_1 = 6;
	public static final int MAHNUNG_1_GEDRUCKT = 7;
	public static final int MAHNUNG_2 = 8;
	public static final int MAHNUNG_2_GEDRUCKT = 9;
	public static final int MAHNUNG_3 = 10;
	public static final int MAHNUNG_3_GEDRUCKT = 11;
	public static final int IN_BETREIBUNG = 12;
	public static final int TEILVERLUST = 13;
	public static final int TOTALVERLUST = 14;
	public static final int TEILZAHLUNG = 15;
	public static final int BEZAHLT = 16;
	public static final int ZUVIEL_BEZAHLT = 17;
	public static final int STORNIERT = 18;
	public static final int VON_HEUTE = 19;
	public static final int NICHT_VON_HEUTE = 20;
	public static final int NICHT_VON_IHNEN = 21;
	public static final int FEHLERHAFT = 22;
	public static final int ZU_DRUCKEN = 23;
	public static final int AUSSTEHEND = 24;
	public static final int MAHNSTOPP = 25;
	public static final int ABGESCHRIEBEN = 26; // Storniert und Kons nicht mehr freigegeben
	public static final int ZURUECKGEWIESEN = 27;
	
	/**
	 * Decide whether this state means an "active" state, i.e. the bill ist not paid or closed by
	 * any means
	 * 
	 * @return true if there are still payments awaited
	 */
	public static boolean isActive(int state){
		if (state > LAUFEND && state < TEILVERLUST) {
			return true;
		}
		if (state == TEILZAHLUNG) {
			return true;
		}
		if (state > FEHLERHAFT && state < ABGESCHRIEBEN) {
			return true;
		}
		return false;
	}
	
	private static final String[] Text = {
		"Unbekannt", "Verrechnet", "Nicht verrechnen", "laufend", "Rn. offen",
		"Offen und gedruckt", "Zahlungserinnerung", "Zahlungserinnerung gedruckt", "2. Mahnung",
		"2. Mahnung gedruckt", "3. Mahnung", "3. Mahnung gedruckt", "In Betreibung", "Teilverlust",
		"Totalverlust", "Teilw. bezahlt", "Bezahlt", "Zuviel bezahlt", "Storniert", "von heute",
		"nicht von heute", "nicht von Ihnen", "fehlerhaft", "zu Drucken", "ausstehend",
		"mahnstopp", "abgeschrieben", "zurückgewiesen"
	};
	
	public static enum REJECTCODE {
		RG_KONS_NO_BILLABLES_NOR_REVENUE, NO_DIAG, NO_MANDATOR, NO_CASE, NO_DEBITOR, NO_GUARANTOR,
			VALIDATION_ERROR, REJECTED_BY_PEER, SUM_MISMATCH, INTERNAL_ERROR;
	};
	
	private static final String[] RejectStrings = {
		"Keine Diagnose", "Kein Rechnungsempfänger", "Kein Garant", "Validierungsfehler",
		"Vom Empfänger zurückgewiesen"
	};
	
	public static String[] getStatusTexts(){
		return Text;
	}
	
	public static String getStatusText(int idx){
		if (idx > 0 && idx < Text.length) {
			return Text[idx];
		}
		return "??";
	}
	
	public static String[] getRejectStrings(){
		return RejectStrings;
	}
	
	public static String getRejectString(REJECTCODE code){
		return RejectStrings[code.ordinal()];
	}
}
