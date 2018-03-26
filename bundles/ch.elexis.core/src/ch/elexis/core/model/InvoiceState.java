package ch.elexis.core.model;

import java.util.ResourceBundle;

import ch.elexis.core.interfaces.ILocalizedEnum;
import ch.elexis.core.interfaces.INumericEnum;

public enum InvoiceState implements INumericEnum, ILocalizedEnum {
	//@formatter:off
	UNKNOWN(0),
	BILLED(1),
	NOT_BILLED(2),
	ONGOING(3),
	OPEN(4),
	OPEN_AND_PRINTED(5),
	DEMAND_NOTE_1(6),
	DEMAND_NOTE_1_PRINTED(7),
	DEMAND_NOTE_2(8),
	DEMAND_NOTE_2_PRINTED(9),
	DEMAND_NOTE_3(10),
	DEMAND_NOTE_3_PRINTED(11),
	IN_EXECUTION(12),
	PARTIAL_LOSS(13),
	TOTAL_LOSS(14),
	PARTIAL_PAYMENT(15),
	PAID(16),
	EXCESSIVE_PAYMENT(17),
	CANCELLED(18),
	FROM_TODAY(19),
	NOT_FROM_TODAY(20),
	NOT_FROM_YOU(21),
	DEFECTIVE(22),
	TO_PRINT(23),
	OWING(24),
	STOP_LEGAL_PROCEEDING(25),
	DEPRECIATED(26), // (Abgeschrieben) Storniert und Kons nicht mehr freigegeben
	REJECTED(27);
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
	 * @return the combined (or) states that represent the owing state
	 */
	public static InvoiceState[] owingStates(){
		return new InvoiceState[] {
			OPEN_AND_PRINTED, DEMAND_NOTE_1_PRINTED, DEMAND_NOTE_2_PRINTED, DEMAND_NOTE_3_PRINTED
		};
	}
	
	/**
	 * 
	 * @return the combined (or) states that represent the to print state
	 */
	public static InvoiceState[] toPrintStates() {
		return new InvoiceState[] {
			OPEN, DEMAND_NOTE_1, DEMAND_NOTE_2, DEMAND_NOTE_3
		};
	}
	
	/**
	 * Decide whether this state means an "active" state, i.e. the bill is not paid or closed by any
	 * means
	 * 
	 * @return true if there are still payments awaited
	 */
	public boolean isActive(){
		if (state > ONGOING.getState() && state < PARTIAL_LOSS.getState()) {
			return true;
		}
		if (state == PARTIAL_PAYMENT.getState()) {
			return true;
		}
		if (state > DEFECTIVE.getState() && state < DEPRECIATED.getState()) {
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
		return InvoiceState.UNKNOWN;
	}

	@Override
	public String getLocaleText(){
		try {
			return ResourceBundle.getBundle(ch.elexis.core.l10n.Messages.BUNDLE_NAME)
					.getString(InvoiceState.class.getSimpleName() + "_" + this.name());
		} catch (Exception e) {
			return this.name();
		}
	}


}
