package ch.elexis.core.ui.contacts;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.l10n.Messages;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.util.FormatValidator;
import ch.elexis.core.ui.util.LabeledInputField;
import ch.elexis.core.ui.util.LabeledInputField.AutoForm;

public class ContactsUtil {

	public static void setEmailValidationListener(AutoForm autoForm) {
		if (autoForm != null && !autoForm.isDisposed()) {
			for (Control child : autoForm.getChildren()) {
				if (child instanceof LabeledInputField
						&& ((LabeledInputField) child).getLabel().equals(Messages.Core_E_Mail)) {
					LabeledInputField field = (LabeledInputField) child;
					if (field.getControl() instanceof Text) {
						((Text) field.getControl()).addModifyListener(e -> {
							field.getControl().setForeground(
									(!field.getText().isEmpty() && !FormatValidator.isValidMailAddress(field.getText())
											? UiDesk.getColor(UiDesk.COL_RED)
											: UiDesk.getColor(UiDesk.COL_BLACK)));
						});
					}
				}
			}
		}
	}
}
