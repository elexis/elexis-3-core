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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.part.ViewPart;
import org.iatrix.help.wiki.Constants;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.rgw.tools.StringTool;

/**
 * Wiki Help Web Browser View.
 * 
 * Shows context sensitive view in a wiki web page.
 * 
 * Interface Specification to Wiki: - The View's class name is used - The dot's are removed - each
 * component is converted to lowercases - the first component character is set to uppercase - the
 * components are assembled to a WikiName - The url is created from the base url and the WikiName
 * 
 * @author Daniel Lutz <danlutz@watz.ch>
 */

public class WikiView extends ViewPart implements ISaveablePart2 {
	public static final String ID = "org.iatrix.help.wiki.views.WikiView"; //$NON-NLS-1$
	
	private Browser browser;
	
	@Override
	public void createPartControl(Composite parent){
		browser = new Browser(parent, SWT.NONE);
		
		initialize();
	}
	
	/**
	 * Set the browser's new page. Does nothing if null is passed as page.
	 * 
	 * @param page
	 *            the new page to set
	 */
	public void setPage(String page){
		if (page != null) {
			String wikiName = getWikiName(page);
			browser.setUrl(getBaseUrl() + wikiName); // ignore errors
		}
	}
	
	/**
	 * Converts name to a wiki name
	 * 
	 * @param name
	 *            the name to be converted
	 * @return the wiki name
	 */
	private String getWikiName(String name){
		// first, replace any special characters by dots
		String normalized = name.replaceAll("[._]+", ".");
		
		// tokenize
		String[] tokens = name.split("[.]");
		
		// convert to upper/lowercase
		for (int i = 0; i < tokens.length; i++) {
			tokens[i] = tokens[i].toLowerCase();
			tokens[i] = tokens[i].substring(0, 1).toUpperCase() + tokens[i].substring(1);
		}
		
		String wikiName = StringTool.join(tokens, "");
		return wikiName;
	}
	
	private String getBaseUrl(){
		return CoreHub.globalCfg.get(Constants.CFG_BASE_URL, Constants.DEFAULT_BASE_URL);
	}
	
	private String getStartPage(){
		return CoreHub.globalCfg.get(Constants.CFG_START_PAGE, Constants.DEFAULT_START_PAGE);
	}
	
	private String getHandbookUri(){
		return CoreHub.globalCfg.get(Constants.CFG_HANDBOOK, Constants.DEFAULT_HANDBOOK);
	}
	
	/**
	 * Sets the initial url
	 */
	public void initialize(){
		setPage(getStartPage());
	}
	
	@Override
	public void setFocus(){
		browser.setFocus();
	}
	
	/* ******
	 * Die folgenden 6 Methoden implementieren das Interface ISaveablePart2 Wir benötigen das
	 * Interface nur, um das Schliessen einer View zu verhindern, wenn die Perspektive fixiert ist.
	 * Gibt es da keine einfachere Methode?
	 */
	public int promptToSaveOnClose(){
		return GlobalActions.fixLayoutAction.isChecked() ? ISaveablePart2.CANCEL
				: ISaveablePart2.NO;
	}
	
	public void doSave(IProgressMonitor monitor){ /* leer */}
	
	public void doSaveAs(){ /* leer */}
	
	public boolean isDirty(){
		return true;
	}
	
	public boolean isSaveAsAllowed(){
		return false;
	}
	
	public boolean isSaveOnCloseNeeded(){
		return true;
	}
}
