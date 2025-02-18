/*******************************************************************************
 * Copyright (c) 2008, Daniel Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel Lutz - initial implementation
 *
 *******************************************************************************/

package org.iatrix.help.wiki.views;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.iatrix.help.wiki.Constants;

import com.equo.chromium.swt.Browser;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.rgw.tools.StringTool;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Wiki Help Web Browser View.
 *
 * Shows context sensitive view in a wiki web page.
 *
 * Interface Specification to Wiki: - The View's class name is used - The dot's
 * are removed - each component is converted to lowercases - the first component
 * character is set to uppercase - the components are assembled to a WikiName -
 * The url is created from the base url and the WikiName
 *
 * @author Daniel Lutz <danlutz@watz.ch>
 */

public class WikiView extends ViewPart {
	public static final String ID = "org.iatrix.help.wiki.views.WikiView"; //$NON-NLS-1$

	private Browser browser;

	@Override
	public void createPartControl(Composite parent) {
		browser = new Browser(parent, SWT.NONE);

		initialize();
	}

	/**
	 * Set the browser's new page. Does nothing if null is passed as page.
	 *
	 * @param page the new page to set
	 */
	public void setPage(String page) {
		if (page != null) {
			String wikiName = getWikiName(page);
			browser.setUrl(getBaseUrl() + wikiName); // ignore errors
		}
	}

	/**
	 * Converts name to a wiki name
	 *
	 * @param name the name to be converted
	 * @return the wiki name
	 */
	private String getWikiName(String name) {
		// first, replace any special characters by dots
		String normalized = name.replaceAll("[._]+", "."); //$NON-NLS-1$ //$NON-NLS-2$

		// tokenize
		String[] tokens = name.split("[.]"); //$NON-NLS-1$

		// convert to upper/lowercase
		for (int i = 0; i < tokens.length; i++) {
			tokens[i] = tokens[i].toLowerCase();
			tokens[i] = tokens[i].substring(0, 1).toUpperCase() + tokens[i].substring(1);
		}

		String wikiName = StringTool.join(tokens, StringUtils.EMPTY);
		return wikiName;
	}

	private String getBaseUrl() {
		return ConfigServiceHolder.getGlobal(Constants.CFG_BASE_URL, Constants.DEFAULT_BASE_URL);
	}

	private String getStartPage() {
		return ConfigServiceHolder.getGlobal(Constants.CFG_START_PAGE, Constants.DEFAULT_START_PAGE);
	}

	private String getHandbookUri() {
		return ConfigServiceHolder.getGlobal(Constants.CFG_HANDBOOK, Constants.DEFAULT_HANDBOOK);
	}

	/**
	 * Sets the initial url
	 */
	public void initialize() {
		setPage(getStartPage());
	}

	@Override
	public void setFocus() {
		browser.setFocus();
	}

	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT) boolean currentState) {
		CoreUiUtil.updateFixLayout(part, currentState);
	}
}
