/*******************************************************************************
 * Copyright (c) 2006-2010, Daniel Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel Lutz - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.core.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.text.EnhancedTextField;
import ch.rgw.io.InMemorySettings;

/**
 * Benutzerspezifische Einstellungen
 */
public class UserTextPref extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	public static final String ID = "ch.elexis.preferences.UserPreferences"; //$NON-NLS-1$
	
	private static final String[] fields = {
		EnhancedTextField.MACRO_KEY
	};
	
	private static final String[] texte = {
		Messages.UserTextPref_MacroKey
	};
	
	public UserTextPref(){
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(new InMemorySettings()));
		setDescription(Messages.UserTextPref_UserPreferences);
	}
	
	@Override
	protected void createFieldEditors(){
		for (int i = 0; i < fields.length; i++) {
			addField(new StringFieldEditor(fields[i], texte[i], getFieldEditorParent()));
		}
	}
	
	public void init(IWorkbench workbench){
		for (String field : fields) {
			String value = CoreHub.userCfg.get(field, EnhancedTextField.MACRO_KEY_DEFAULT);
			getPreferenceStore().setValue(field, value);
		}
	}
	
	@Override
	public boolean performOk(){
		super.performOk();
		
		for (String field : fields) {
			String value = getPreferenceStore().getString(field);
			CoreHub.userCfg.set(field, value);
		}
		
		return true;
	}
	
	@Override
	protected void performDefaults(){
		this.initialize();
	}
}
