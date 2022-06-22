package ch.elexis.core.ui.util;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;

public class FilterNonPrintableModifyListener implements ModifyListener {

	public static FilterNonPrintableModifyListener addTo(Text text) {
		FilterNonPrintableModifyListener ret = new FilterNonPrintableModifyListener();
		ret.text = text;
		text.addModifyListener(ret);
		return ret;
	}

	public static String filterNonPrintable(String input) {
		return input.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
	}

	private Text text;

	@Override
	public void modifyText(ModifyEvent e) {
		if (StringUtils.isNotBlank(text.getText())) {
			String noControlChar = filterNonPrintable(text.getText());
			if (noControlChar.toCharArray().length != text.getText().toCharArray().length) {
				text.setText(noControlChar);
			}
		}
	}
}
