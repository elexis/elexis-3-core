package ch.elexis.core.model.esr;

import java.util.HashMap;

import ch.elexis.core.interfaces.INumericEnum;

public enum ESRCode implements INumericEnum {
	//@formatter:off
	Gutschrift_edv(0),
	Storno_edv(1),
	Korrektur_edv(2),
	Gutschrift_Schalter(3),
	Storno_Schalter(4),
	Korrektur_Schalter(5),
	Summenrecord(6),
	Unbekannt(7);
	//@formatter:on

	private int numeric;

	private static HashMap<Integer, ESRCode> numericMap = new HashMap<>();

	ESRCode(int numeric) {
		this.numeric = numeric;
	}

	@Override
	public int numericValue() {
		return numeric;
	}

	public static ESRCode byNumeric(int numeric) {
		if (numericMap.isEmpty()) {
			ESRCode[] entries = values();
			for (int i = 0; i < entries.length; i++) {
				numericMap.put(entries[i].numericValue(), entries[i]);
			}
		}
		return numericMap.getOrDefault(numeric, Unbekannt);
	}
}
