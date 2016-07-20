package ch.elexis.core.model;

import ch.elexis.core.interfaces.INumericEnum;

public enum InvoiceState implements INumericEnum {
	//@formatter:off
	UNBEKANNT(0),
	VERRECHNET(1),
	NICHT_VERRECHNET(2),
	LAUFEND(3),
	OFFEN(4),
	OFFEN_UND_GEDRUCKT(5),
	MAHNUNG_1(6),
	MAHNUNG_1_GEDRUCKT(7),
	MAHNUNG_2(8),
	MAHNUNG_2_GEDRUCKT(9),
	MAHNUNG_3(10),
	MAHNUNG_3_GEDRUCKT(11),
	IN_BETREIBUNG(12),
	TEILVERLUST(13),
	TOTALVERLUST(14),
	TEILZAHLUNG(15),
	BEZAHLT(16),
	ZUVIEL_BEZAHLT(17),
	STORNIERT(18),
	VON_HEUTE(19),
	NICHT_VON_HEUTE(20),
	NICHT_VON_IHNEN(21),
	FEHLERHAFT(22),
	ZU_DRUCKEN(23),
	AUSSTEHEND(24),
	MAHNSTOPP(25),
	ABGESCHRIEBEN(26), // Storniert und Kons nicht mehr freigegeben
	ZURUECKGEWIESEN(27);
	//@formatter:on
	
	private int state;
	
	public static enum REJECTCODE {
			RG_KONS_NO_BILLABLES_NOR_REVENUE, NO_DIAG, NO_MANDATOR, NO_CASE, NO_DEBITOR,
			NO_GUARANTOR, VALIDATION_ERROR, REJECTED_BY_PEER, SUM_MISMATCH, INTERNAL_ERROR;
	};
	
	private InvoiceState(int state){
		this.state = state;
	}
	
	public int getState(){
		return state;
	}
	
	@Override
	public int numericValue(){
		return state;
	}
	
	/**
	 * Decide whether this state means an "active" state, i.e. the bill is not paid or closed by any
	 * means
	 * 
	 * @return true if there are still payments awaited
	 */
	public boolean isActive(){
		if (state > LAUFEND.getState() && state < TEILVERLUST.getState()) {
			return true;
		}
		if (state == TEILZAHLUNG.getState()) {
			return true;
		}
		if (state > FEHLERHAFT.getState() && state < ABGESCHRIEBEN.getState()) {
			return true;
		}
		return false;
	}
	
	public static InvoiceState fromState(int value){
		for (InvoiceState is : InvoiceState.values()) {
			if (value == is.getState()) {
				return is;
			}
		}
		return InvoiceState.UNBEKANNT;
	}


}
