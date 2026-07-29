package ch.elexis.core.ui.contacts.views.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Combo;
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
		applyBirthdateFilterFormatting(field, false);
	}

	public static void applyBirthdateFilterFormatting(Text field, boolean autoInsertSeparators) {
		if (field == null || field.isDisposed()) {
			return;
		}
		field.setTextLimit(MAX_DOB_LENGTH);
		field.addModifyListener(new BirthdateFilterModifyListener(field, autoInsertSeparators));
	}

	public static void restrictToValues(Combo combo, String... allowedValues) {
		addValueRestriction(combo, false, allowedValues);
	}

	public static void restrictToValuesIgnoreCase(Combo combo, String... allowedValues) {
		addValueRestriction(combo, true, allowedValues);
	}

	public static void disableMouseWheel(Combo combo) {
		if (combo == null || combo.isDisposed()) {
			return;
		}
		combo.addListener(SWT.MouseWheel, event -> event.doit = false);
	}

	private static void addValueRestriction(Combo combo, boolean ignoreCase, String... allowedValues) {
		if (combo == null || combo.isDisposed() || allowedValues == null || allowedValues.length == 0) {
			return;
		}
		combo.addVerifyListener(new ValueRestrictionVerifyListener(combo, ignoreCase, allowedValues));
	}

	private static final class ValueRestrictionVerifyListener implements VerifyListener {

		private final Combo combo;
		private final boolean ignoreCase;
		private final String[] allowedValues;

		private ValueRestrictionVerifyListener(Combo combo, boolean ignoreCase, String[] allowedValues) {
			this.combo = combo;
			this.ignoreCase = ignoreCase;
			this.allowedValues = allowedValues;
		}

		@Override
		public void verifyText(VerifyEvent event) {
			String current = combo.getText();
			String result = current.substring(0, event.start) + event.text + current.substring(event.end);
			if (result.isEmpty()) {
				return;
			}
			for (String allowedValue : allowedValues) {
				if (ignoreCase ? result.equalsIgnoreCase(allowedValue) : result.equals(allowedValue)) {
					return;
				}
			}
			event.doit = false;
		}
	}

	private static final class BirthdateFilterModifyListener implements ModifyListener {

		private final Text field;
		private final boolean autoInsertSeparators;
		private boolean reformatting = false;
		private int previousLength = 0;

		private BirthdateFilterModifyListener(Text field, boolean autoInsertSeparators) {
			this.field = field;
			this.autoInsertSeparators = autoInsertSeparators;
			this.previousLength = field.getText().length();
		}

		@Override
		public void modifyText(ModifyEvent e) {
			if (reformatting) {
				return;
			}
			String current = field.getText();
			boolean removing = current.length() < previousLength;
			previousLength = current.length();
			if (autoInsertSeparators && removing) {
				// do not add separators the user is currently deleting
				return;
			}
			String formatted = autoInsertSeparators ? FormatValidator.getFormattedBirthdate(current)
					: FormatValidator.getFormattedBirthdateFilter(current);
			if (!formatted.equals(current)) {
				reformatting = true;
				field.setText(formatted);
				field.setSelection(formatted.length());
				reformatting = false;
				previousLength = formatted.length();
			}
		}
	}
}
