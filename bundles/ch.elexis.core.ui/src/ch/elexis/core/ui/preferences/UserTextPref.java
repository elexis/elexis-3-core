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

import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.preferences.ConfigServicePreferenceStore.Scope;
import ch.elexis.core.ui.text.EnhancedTextField;

/**
 * Benutzerspezifische Einstellungen
 */
public class UserTextPref extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static final String ID = "ch.elexis.preferences.UserPreferences"; //$NON-NLS-1$

	private static final String[] fields = { EnhancedTextField.MACRO_KEY };

	private static final String[] texte = { Messages.UserTextPref_MacroKey };

	private HashMap<String, String> makros = new HashMap<String, String>();

	public UserTextPref() {
		super(GRID);
		setPreferenceStore(new ConfigServicePreferenceStore(Scope.USER));
		setDescription(Messages.UserTextPref_UserPreferences);
	}

	@Override
	protected void createFieldEditors() {
		for (int i = 0; i < fields.length; i++) {
			addField(new StringFieldEditor(fields[i], texte[i], getFieldEditorParent()));
		}
		Set<String> makroNames = makros.keySet();
		for (String name : makroNames) {
			addField(new BooleanFieldEditor(EnhancedTextField.MACRO_ENABLED + "/" + makros.get(name), name, //$NON-NLS-1$
					getFieldEditorParent()));
		}
	}

	@Override
	public void init(IWorkbench workbench) {
		for (String field : fields) {
			String value = ConfigServiceHolder.getUser(field, EnhancedTextField.MACRO_KEY_DEFAULT);
			getPreferenceStore().setValue(field, value);
		}

		setMakroEnabledDefaults();

		List<IConfigurationElement> makroExtensions = Extensions.getExtensions(ExtensionPointConstantsUi.KONSEXTENSION);
		for (IConfigurationElement iConfigurationElement : makroExtensions) {
			String name = iConfigurationElement.getAttribute("name"); //$NON-NLS-1$
			String clazz = iConfigurationElement.getAttribute("KonsMakro"); //$NON-NLS-1$
			if (clazz != null && !clazz.isEmpty() && !"enabled".equals(name)) { //$NON-NLS-1$
				makros.put(name, clazz);
			}
		}
	}

	/**
	 * Set default enabled preferences to false for the makro extensions.
	 *
	 * @since 3.1
	 */
	public static void setMakroEnabledDefaults() {
		List<IConfigurationElement> makroExtensions = Extensions.getExtensions(ExtensionPointConstantsUi.KONSEXTENSION);
		for (IConfigurationElement iConfigurationElement : makroExtensions) {
			String name = iConfigurationElement.getAttribute("name"); //$NON-NLS-1$
			String clazz = iConfigurationElement.getAttribute("KonsMakro"); //$NON-NLS-1$
			if (clazz != null && !clazz.isEmpty() && !"enabled".equals(name)) { //$NON-NLS-1$
				boolean enabled = ConfigServiceHolder.getUser(EnhancedTextField.MACRO_ENABLED + "/" + clazz, false); //$NON-NLS-1$
				// set disabled as default ...
				if (!enabled) {
					ConfigServiceHolder.setUser(EnhancedTextField.MACRO_ENABLED + "/" + clazz, false); //$NON-NLS-1$
				}
			} else if (clazz != null && !clazz.isEmpty() && "enabled".equals(name)) { //$NON-NLS-1$
				// set enabled for makros with name enabled
				ConfigServiceHolder.setUser(EnhancedTextField.MACRO_ENABLED + "/" + clazz, true); //$NON-NLS-1$
			}
		}
	}

	@Override
	protected void performDefaults() {
		this.initialize();
	}
}
