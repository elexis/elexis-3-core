/*******************************************************************************
 * Copyright (c) 2008, D. Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    D. Lutz - initial implementation
 *    
 *******************************************************************************/

package org.iatrix.help.wiki.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.iatrix.help.wiki.Constants;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.preferences.SettingsPreferenceStore;

public class WikiPreferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	private SettingsPreferenceStore prefs = new SettingsPreferenceStore(CoreHub.globalCfg);
	
	public WikiPreferences(){
		super(GRID);
		setPreferenceStore(prefs);
		prefs.setDefault(Constants.CFG_BASE_URL, Constants.DEFAULT_BASE_URL);
		prefs.setDefault(Constants.CFG_START_PAGE, Constants.DEFAULT_START_PAGE);
		prefs.setDefault(Constants.CFG_HANDBOOK, Constants.DEFAULT_HANDBOOK);
	}
	
	@Override
	protected void createFieldEditors(){
		addField(new StringFieldEditor(Constants.CFG_BASE_URL, "Basis-URL", getFieldEditorParent()));
		addField(new StringFieldEditor(Constants.CFG_START_PAGE, "Start-Seite",
			getFieldEditorParent()));
		addField(new StringFieldEditor(Constants.CFG_HANDBOOK, "Handbuch", getFieldEditorParent()));
	}
	
	public void init(IWorkbench workbench){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean performOk(){
		prefs.flush();
		return super.performOk();
	}
}
