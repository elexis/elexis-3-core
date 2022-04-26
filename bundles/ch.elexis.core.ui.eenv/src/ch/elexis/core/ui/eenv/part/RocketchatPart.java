package ch.elexis.core.ui.eenv.part;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.equo.chromium.swt.Browser;

import ch.elexis.core.eenv.IElexisEnvironmentService;

public class RocketchatPart {

	@Inject
	@Optional
	private IElexisEnvironmentService elexisEnvironmentService;

	private Browser browser;

	/**
	 * Create contents of the view part.
	 */
	@PostConstruct
	public void createControls(Composite parent) {

		browser = new Browser(parent, SWT.NONE);
		if (elexisEnvironmentService == null) {
			browser.setText("Elexis-Environment nicht konfiguriert");
		} else {
			// browser.setCookie(value, url)
			// login cookies?!
			browser.setUrl(elexisEnvironmentService.getRocketchatBaseUrl());
		}

	}

	@Focus
	public void setFocus() {
		browser.setFocus();
		System.out.println(browser.getBrowserType());
	}

}
