package ch.elexis.core.ui.util;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.model.format.FormatValidator;
import ch.elexis.core.ui.UiDesk;

public class EmailValidationModifyListener implements ModifyListener {

	private Text text;

	public static EmailValidationModifyListener addTo(Text text) {
		EmailValidationModifyListener listener = new EmailValidationModifyListener();
		listener.text = text;
		text.addModifyListener(listener);
		return listener;
	}

	@Override
	public void modifyText(ModifyEvent e) {
		text.setForeground(
				(StringUtils.isNotBlank((text.getText())) && !FormatValidator.isValidMailAddress(text.getText()))
						? UiDesk.getColor(UiDesk.COL_RED)
						: UiDesk.getColor(UiDesk.COL_BLACK));
	}

}
