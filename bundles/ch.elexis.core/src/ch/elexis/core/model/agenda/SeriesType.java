package ch.elexis.core.model.agenda;

public enum SeriesType {
	DAILY('D'), WEEKLY('W'), MONTHLY('M'), YEARLY('Y');

	private char seriesTypeCharacter;

	private SeriesType(char seriesTypeCharacter) {
		this.seriesTypeCharacter = seriesTypeCharacter;
	}

	public char getSeriesTypeCharacter() {
		return seriesTypeCharacter;
	}

	public static SeriesType getForCharacter(char c) {
		switch (c) {
		case 'D':
			return SeriesType.DAILY;
		case 'W':
			return SeriesType.WEEKLY;
		case 'M':
			return SeriesType.MONTHLY;
		case 'Y':
			return SeriesType.YEARLY;
		default:
			return null;
		}
	}
}
