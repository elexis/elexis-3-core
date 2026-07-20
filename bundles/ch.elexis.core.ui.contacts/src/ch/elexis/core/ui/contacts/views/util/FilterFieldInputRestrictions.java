package ch.elexis.core.ui.contacts.views.util;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.model.format.FormatValidator;

public final class FilterFieldInputRestrictions {

	private static final int MAX_DOB_LENGTH = 10;

	private FilterFieldInputRestrictions() {
		// utility class
	}

	public static void restrictToDigits(Text field) {
		if (field == null || field.isDisposed()) {
			return;
		}
		field.addVerifyListener(event -> {
			for (int c = 0; c < event.text.length(); c++) {
				if (!Character.isDigit(event.text.charAt(c))) {
					event.doit = false;
					return;
				}
			}
		});
	}

	public static void applyBirthdateFilterFormatting(Text field) {
		if (field == null || field.isDisposed()) {
			return;
		}
		field.setTextLimit(MAX_DOB_LENGTH);
		field.addModifyListener(new BirthdateFilterModifyListener(field));
	}

	private static final class BirthdateFilterModifyListener implements ModifyListener {

		private final Text field;
		private boolean reformatting = false;

		private BirthdateFilterModifyListener(Text field) {
			this.field = field;
		}

		@Override
		public void modifyText(ModifyEvent e) {
			if (reformatting) {
				return;
			}
			String current = field.getText();
			String formatted = FormatValidator.getFormattedBirthdateFilter(current);
			if (!formatted.equals(current)) {
				reformatting = true;
				field.setText(formatted);
				field.setSelection(formatted.length());
				reformatting = false;
			}
		}
	}
}
