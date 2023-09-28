package ch.elexis.core.model.esr;

import java.util.HashMap;

import ch.elexis.core.interfaces.INumericEnum;

public enum ESRRejectCode implements INumericEnum {
	//@formatter:off
	OK(0),
	ESRREJECT(1),
	MASSENREJECT(2),
	BETRAG(3),
	MANDANT(4),
	RN_NUMMER(5),
	PAT_NUMMER(6),
	DUPLIKAT(7),
	ANDERE(8),
	PAT_FALSCH(9),
	REFERNECE(10);
	//@formatter:on

	private int numeric;

	private static HashMap<Integer, ESRRejectCode> numericMap = new HashMap<>();

	ESRRejectCode(int numeric) {
		this.numeric = numeric;
	}

	@Override
	public int numericValue() {
		return numeric;
	}

	public static ESRRejectCode byNumeric(int numeric) {
		if (numericMap.isEmpty()) {
			ESRRejectCode[] entries = values();
			for (int i = 0; i < entries.length; i++) {
				numericMap.put(entries[i].numericValue(), entries[i]);
			}
		}
		return numericMap.getOrDefault(numeric, ANDERE);
	}
}
