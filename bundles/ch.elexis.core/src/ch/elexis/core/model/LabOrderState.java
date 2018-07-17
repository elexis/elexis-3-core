package ch.elexis.core.model;

public enum LabOrderState {
		ORDERED(0), DONE(1), DONE_IMPORT(2);
	
	private final Integer value;
	
	private LabOrderState(Integer value){
		this.value = value;
	}
	
	public Integer getValue(){
		return value;
	}
	
	public static LabOrderState ofValue(int value){
		for (LabOrderState state : values()) {
			if(state.getValue() == value) {
				return state;
			}
		}
		return null;
	}
}
