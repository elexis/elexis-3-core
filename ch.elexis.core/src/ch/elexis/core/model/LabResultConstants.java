package ch.elexis.core.model;

public class LabResultConstants {
	public static final int PATHOLOGIC = 1 << 0;
	public static final int OBSERVE = 1 << 1; // Anwender erklärt den Parameter für
	// beobachtungswürdig
	public static final int NORMAL = 1 << 2; // Anwender erklärt den Wert explizit für normal (auch
	// wenn er formal ausserhalb des Normbereichs ist)
}
