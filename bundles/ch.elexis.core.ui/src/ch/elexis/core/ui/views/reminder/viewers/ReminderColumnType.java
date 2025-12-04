package ch.elexis.core.ui.views.reminder.viewers;

import org.eclipse.swt.graphics.Color;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.UiDesk;

public enum ReminderColumnType {

	TYPE(Messages.Core_Type, 20), DATE(Messages.Core_Date, 70), RESPONSIBLE(Messages.EditReminderDialog_assigTo, 100),
	STATUS(Messages.Core_Status, 100), PATIENT(Messages.Core_Patient, 145), DESCRIPTION(Messages.Core_Subject, 400);

	private final String title;
	private final int defaultWidth;

	ReminderColumnType(String title, int defaultWidth) {
		this.title = title;
		this.defaultWidth = defaultWidth;
	}

	public String getTitle() {
		return title;
	}

	public int getDefaultWidth() {
		return defaultWidth;
	}

	public static String[] getAllTitles() {
		String[] titles = new String[values().length];
		for (int i = 0; i < values().length; i++) {
			titles[i] = values()[i].getTitle();
		}
		return titles;
	}

	public enum ReminderColorType {

		IN_PROGRESS("IN_PROGRESS", "FFFFFF"), DUE("DUE", "FFFFFF"), OVERDUE("OVERDUE", "FFFFFF"),
		OPEN("OPEN", "FFFFFF"), CLOSED("CLOSED", "FFFFFF");

		private final String key;
		private final String defaultRGB;

		ReminderColorType(String key, String defaultRGB) {
			this.key = key;
			this.defaultRGB = defaultRGB;
		}


		public Color getColor() {
			String prefPath = Preferences.USR_REMINDERCOLORS + "/" + key;
			String rgb = ConfigServiceHolder.getUser(prefPath, defaultRGB);
			return UiDesk.getColorFromRGB(rgb);
		}
	}
}
