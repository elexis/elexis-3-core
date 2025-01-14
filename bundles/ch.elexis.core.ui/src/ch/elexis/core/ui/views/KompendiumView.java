/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich, Daniel Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel Lutz - initial implementation based on RnPrintView
 *
 *******************************************************************************/

package ch.elexis.core.ui.views;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.equo.chromium.swt.Browser;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Diese View reichtet einen Browser aufs Arzneimittel-Kompendium ein.
 */
public class KompendiumView extends ViewPart {
	public static final String ID = "ch.elexis.Kompendium"; //$NON-NLS-1$
	static Browser browser;

	@Override
	public void createPartControl(Composite parent) {
		browser = new Browser(parent, SWT.NONE);
		browser.addLocationListener(new LocationAdapter() {

			@Override
			public void changed(LocationEvent arg0) {
				String text = browser.getText();
				// System.out.println(text);
			}

		});
		browser.setUrl("http://www.compendium.ch/search/de"); //$NON-NLS-1$

	}

	public static String getText() {
		return browser.getText();
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT) boolean currentState) {
		CoreUiUtil.updateFixLayout(part, currentState);
	}
}
