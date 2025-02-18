/*******************************************************************************
 * Copyright (c) 2005-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *
 *******************************************************************************/
package ch.elexis.core.ui.views;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.lang3.StringUtils;
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
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Diese View reichtet einen Browser aufs Arzneimittel-Kompendium ein.
 */
public class ODDBView extends ViewPart {
	public static final String ID = "ch.elexis.ODDBView"; //$NON-NLS-1$
	Browser browser;

	@Override
	public void createPartControl(Composite parent) {
		browser = new Browser(parent, SWT.NONE);
		browser.addLocationListener(new LocationAdapter() {

			@Override
			public void changed(LocationEvent arg0) {
				String text = getText(arg0.location);
				System.out.println(text);
			}

		});
		// browser.setUrl("http://ch.oddb.org");
		browser.setUrl("https://just-medical.oddb.org/"); //$NON-NLS-1$

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public String getText(String loc) {
		try {
			if (StringTool.isNothing(loc)) {
				loc = browser.getUrl();
			}
			URLConnection url = new URL(loc).openConnection();
			url.setDoInput(true);
			BufferedReader in = new BufferedReader(new InputStreamReader(url.getInputStream()));
			StringBuilder ret = new StringBuilder();
			String line;
			;
			while ((line = in.readLine()) != null) {
				ret.append(line);
			}
			// Programm beenden
			in.close();
			return ret.toString();
		} catch (IOException e) {
			ExHandler.handle(e);
			return StringUtils.EMPTY;
		}
	}

	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT) boolean currentState) {
		CoreUiUtil.updateFixLayout(part, currentState);
	}
}
