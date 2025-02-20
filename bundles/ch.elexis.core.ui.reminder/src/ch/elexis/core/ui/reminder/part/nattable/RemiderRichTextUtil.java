package ch.elexis.core.ui.reminder.part.nattable;

import java.time.format.DateTimeFormatter;

import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.issue.Priority;

public class RemiderRichTextUtil {

	public static String richText(IReminder reminder, boolean addDate) {
		StringBuilder sb = new StringBuilder();
		if (reminder.getPriority() == Priority.HIGH) {
			sb.append("<strong><span style=\"font-size: 14; color:rgb(255,0,0);\">");
			sb.append(" !! ");
			sb.append("</span></strong>");
		}
		sb.append("<strong>" + reminder.getSubject() + "</strong>");
		if (addDate && reminder.getDue() != null) {
			sb.append(" " + DateTimeFormatter.ofPattern("dd.MM.yyyy").format(reminder.getDue()));
		}
		if (reminder.getContact() != null && reminder.getContact().isPatient()) {
			sb.append("<br />").append(reminder.getContact().getLabel());
		}
		return sb.toString();
	}

	public static String richText(String string) {
		StringBuilder sb = new StringBuilder();
		sb.append("<strong>" + string + "</strong>");
		return sb.toString();
	}

	public static String richText(String string, int increaseFontSize) {
		StringBuilder sb = new StringBuilder();
		int size = 14 + increaseFontSize;
		sb.append("<p style=\"width: 100%;text-align: center\">");
		sb.append("<span style=\"font-size: " + size + "\">");
		sb.append("<strong>" + string + "</strong>");
		sb.append("</span>");
		sb.append("</p>");
		return sb.toString();
	}
}
