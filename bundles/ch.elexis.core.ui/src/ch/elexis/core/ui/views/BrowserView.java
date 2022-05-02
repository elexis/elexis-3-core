package ch.elexis.core.ui.views;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;

import com.equo.chromium.swt.Browser;

public class BrowserView extends ViewPart {

	public static final String ID = "ch.elexis.core.ui.chromium.views.BrowserView";

	private Browser browser;

	@Override
	public void createPartControl(Composite parent) {
		browser = new Browser(parent, SWT.NONE);
		browser.setUrl("http://www.google.com");
	}

	@Override
	public void setFocus() {
	}

	public void navigateTo() {
		InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(), StringUtils.EMPTY, "Adresse eingeben",
				browser.getUrl(), null);
		if (dlg.open() == Window.OK) {
			browser.setUrl(dlg.getValue());
		}
	}
}
