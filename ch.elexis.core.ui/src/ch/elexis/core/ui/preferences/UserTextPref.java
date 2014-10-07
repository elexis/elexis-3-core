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

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.text.EnhancedTextField;

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
	
	private HashMap<String, String> makros = new HashMap<String, String>();

	public UserTextPref(){
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.userCfg));
		setDescription(Messages.UserTextPref_UserPreferences);
	}
	
	@Override
	protected void createFieldEditors(){
		for (int i = 0; i < fields.length; i++) {
			addField(new StringFieldEditor(fields[i], texte[i], getFieldEditorParent()));
		}
		Set<String> makroNames = makros.keySet();
		for (String name : makroNames) {
			addField(new BooleanFieldEditor(EnhancedTextField.MACRO_ENABLED + "/"
				+ makros.get(name), name, getFieldEditorParent()));
		}
	}
	
	@Override
	public void init(IWorkbench workbench){
		for (String field : fields) {
			String value = CoreHub.userCfg.get(field, EnhancedTextField.MACRO_KEY_DEFAULT);
			getPreferenceStore().setValue(field, value);
		}
		
		setMakroEnabledDefaults();

		List<IConfigurationElement> makroExtensions =
			Extensions.getExtensions(ExtensionPointConstantsUi.KONSEXTENSION);
		for (IConfigurationElement iConfigurationElement : makroExtensions) {
			String name = iConfigurationElement.getAttribute("name");
			String clazz = iConfigurationElement.getAttribute("KonsMakro");
			if (clazz != null && !clazz.isEmpty() && !name.equals("enabled")) {
				makros.put(name, clazz);
			}
		}
	}
	
	public static void setMakroEnabledDefaults(){
		List<IConfigurationElement> makroExtensions =
			Extensions.getExtensions(ExtensionPointConstantsUi.KONSEXTENSION);
		for (IConfigurationElement iConfigurationElement : makroExtensions) {
			String name = iConfigurationElement.getAttribute("name");
			String clazz = iConfigurationElement.getAttribute("KonsMakro");
			if (clazz != null && !clazz.isEmpty() && !name.equals("enabled")) {
				boolean enabled =
					CoreHub.userCfg.get(EnhancedTextField.MACRO_ENABLED + "/" + clazz, false);
				// set disabled as default ...
				if (!enabled) {
					CoreHub.userCfg.set(EnhancedTextField.MACRO_ENABLED + "/" + clazz, false);
				}
			} else if (clazz != null && !clazz.isEmpty() && name.equals("enabled")) {
				// set enabled for makros with name enabled
				CoreHub.userCfg.set(EnhancedTextField.MACRO_ENABLED + "/" + clazz, true);
			}
		}
	}

	@Override
	protected void performDefaults(){
		this.initialize();
	}
}
