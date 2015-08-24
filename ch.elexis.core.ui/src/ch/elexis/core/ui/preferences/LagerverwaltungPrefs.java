/*******************************************************************************
 * Copyright (c) 2005-2008, D. Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    D. Lutz - initial implementation
 *    G. Weirich check illegal values
 *    
 *******************************************************************************/
package ch.elexis.core.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;

/**
 * Einstellungen für die Lagerverwaltung
 * 
 * @author Daniel Lutz <danlutz@watz.ch>
 */
public class LagerverwaltungPrefs extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {
	
	public LagerverwaltungPrefs(){
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.globalCfg));
		setDescription(Messages.LagerverwaltungPrefs_storageManagement);
		getPreferenceStore().setDefault(Preferences.INVENTORY_CHECK_ILLEGAL_VALUES,
			Preferences.INVENTORY_CHECK_ILLEGAL_VALUES_DEFAULT);
		getPreferenceStore().setDefault(Preferences.INVENTORY_MARK_AS_ORDERED,
			Preferences.INVENTORY_MARK_AS_ORDERED_DEFAULT);
	}
	
	@Override
	protected void createFieldEditors(){
		addField(new BooleanFieldEditor(Preferences.INVENTORY_CHECK_ILLEGAL_VALUES,
			Messages.LagerverwaltungPrefs_checkForInvalid, getFieldEditorParent()));
		addField(new RadioGroupFieldEditor(Preferences.INVENTORY_ORDER_TRIGGER,
			Messages.LagerverwaltungPrefs_orderCriteria, 1, new String[][] {
				{
					Messages.LagerverwaltungPrefs_orderWhenBelowMi,
					Preferences.INVENTORY_ORDER_TRIGGER_BELOW_VALUE
				},
				{
					Messages.LagerverwaltungPrefs_orderWhenAtMin,
					Preferences.INVENTORY_ORDER_TRIGGER_EQUAL_VALUE
				},
			}, getFieldEditorParent()));
		
		addField(new BooleanFieldEditor(Preferences.INVENTORY_MARK_AS_ORDERED,
			Messages.LagerverwaltungPrefs_markOrdered, getFieldEditorParent()));
	}
	
	public void init(final IWorkbench workbench){}
	
	@Override
	public boolean performOk(){
		if (super.performOk()) {
			CoreHub.globalCfg.flush();
			return true;
		}
		return false;
	}
}
