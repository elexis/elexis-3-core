package ch.elexis.core.model.prescription;

import java.util.HashMap;

import ch.elexis.core.interfaces.INumericEnum;

/**
 * The allowed prescription and disposal types  @since 3.1.0
 */
public enum EntryType implements INumericEnum {
	//@formatter:off
	UNKNOWN(-1),
	/** Medicine to take over a longer period. <br> i.e. against too high blood pressure, heart medicine **/
	FIXED_MEDICATION (0), 
	/** Medicine given in case a need occurs."Reservemedikation" <br>i.e. patient plans a journey and gets medicine against pain, sickness, insect bites to take in case something happens  **/
	RESERVE_MEDICATION (1), 
	/** Written a recipe for this medicine **/
	RECIPE (2),
	/** For self dispensation **/
	SELF_DISPENSED (3), 
	/** Medicine given because of a current problem, but not intended for a longer period **/
	SYMPTOMATIC_MEDICATION(5);
	//@formatter:on
	
	private int numeric;
	
	private static HashMap<Integer, EntryType> numericMap = new HashMap<>();
	
	private EntryType(int numeric){
		this.numeric = numeric;
	}
	
	@Override
	public int numericValue(){
		return numeric;
	}
	
	public static EntryType byNumeric(int numeric){
		if(numericMap.isEmpty()) {
			EntryType[] entries = values();
			for (int i = 0; i< entries.length ; i++) {
				numericMap.put(entries[i].numericValue(), entries[i]);
			}
		}
		return numericMap.getOrDefault(numeric, UNKNOWN);
	}
}
