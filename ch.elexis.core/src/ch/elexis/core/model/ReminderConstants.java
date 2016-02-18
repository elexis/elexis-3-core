package ch.elexis.core.model;

public class ReminderConstants {
	public enum Typ {
		anzeigeTodoPat, anzeigeTodoAll, anzeigeOeffnen, anzeigeProgstart, brief
	}
	
	public enum Status {
		STATE_PLANNED, STATE_DUE, STATE_OVERDUE, STATE_DONE, STATE_UNDONE
	}
}
