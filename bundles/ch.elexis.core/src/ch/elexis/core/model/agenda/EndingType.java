package ch.elexis.core.model.agenda;

public enum EndingType {
	AFTER_N_OCCURENCES('O'), ON_SPECIFIC_DATE('D');
	
	private char endingTypeChar;
	
	private EndingType(char endingTypeChar){
		this.endingTypeChar = endingTypeChar;
	}
	
	public char getEndingTypeChar(){
		return endingTypeChar;
	}
	
	public static EndingType getForCharacter(char c){
		switch (c) {
		case 'O':
			return EndingType.AFTER_N_OCCURENCES;
		case 'D':
			return EndingType.ON_SPECIFIC_DATE;
		default:
			return null;
		}
	}
}
