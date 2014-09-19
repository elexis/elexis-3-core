/*******************************************************************************
 * Copyright (c) 2008-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.contacts.preferences;

import static ch.elexis.core.ui.constants.UiPreferenceConstants.USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_CLOSED;
import static ch.elexis.core.ui.constants.UiPreferenceConstants.USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_OPEN;
import static ch.elexis.core.ui.constants.UiPreferenceConstants.USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_REMEMBER_STATE;
import static ch.elexis.core.ui.constants.UiPreferenceConstants.USERSETTINGS2_EXPANDABLE_COMPOSITES;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.constants.UiResourceConstants;
import ch.elexis.core.ui.contacts.views.Patientenblatt2;
import ch.elexis.core.ui.preferences.Messages;
import ch.elexis.core.ui.preferences.SettingsPreferenceStore;
import ch.elexis.core.ui.preferences.inputs.ComboFieldEditor;
import ch.elexis.core.ui.preferences.inputs.MultilineFieldEditor;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Patient;
import ch.rgw.tools.StringTool;

public class UserSettings2 extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public static final String REMEMBER_LAST_STATE = Messages.UserSettings2_RememberLastState;
	public static final String ALWAYS_CLOSED = Messages.UserSettings2_AlwaysClosed;
	public static final String ALWAYS_OPEN = Messages.UserSettings2_AlwaysOpen;
	
	private SettingsPreferenceStore prefs = new SettingsPreferenceStore(CoreHub.userCfg);
	
	private static final String[] patlistFocusFields = {
		Patient.FLD_PATID, Patient.FLD_NAME, Patient.FLD_FIRSTNAME, Patient.BIRTHDATE,
	};
	
	public UserSettings2(){
		super(GRID);
		setPreferenceStore(prefs);
		prefs.setDefault(USERSETTINGS2_EXPANDABLE_COMPOSITES,
			USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_REMEMBER_STATE);
		prefs.setDefault(Preferences.USR_PATLIST_SHOWPATNR, false);
		prefs.setDefault(Preferences.USR_PATLIST_SHOWNAME, true);
		prefs.setDefault(Preferences.USR_PATLIST_SHOWFIRSTNAME, true);
		prefs.setDefault(Preferences.USR_PATLIST_SHOWDOB, true);
	}
	
	@Override
	protected void createFieldEditors(){
		addField(new RadioGroupFieldEditor(USERSETTINGS2_EXPANDABLE_COMPOSITES,
			Messages.UserSettings2_ExtendableFields, 1, new String[][] {
				{
					ALWAYS_OPEN, USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_OPEN
				}, {
					ALWAYS_CLOSED, USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_CLOSED
				}, {
					REMEMBER_LAST_STATE, USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_REMEMBER_STATE
				}
			
			}, getFieldEditorParent()));
		new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(SWTHelper
			.getFillGridData(2, true, 1, false));
		new Label(getFieldEditorParent(), SWT.NONE).setText(Messages.UserSettings2_FieldsInList);
		addField(new BooleanFieldEditor(Preferences.USR_PATLIST_SHOWPATNR,
			Messages.UserSettings2_PatientNr, getFieldEditorParent()));
		addField(new BooleanFieldEditor(Preferences.USR_PATLIST_SHOWNAME,
			Messages.UserSettings2_PatientName, getFieldEditorParent()));
		addField(new BooleanFieldEditor(Preferences.USR_PATLIST_SHOWFIRSTNAME,
			Messages.UserSettings2_PatientFirstname, getFieldEditorParent()));
		addField(new BooleanFieldEditor(Preferences.USR_PATLIST_SHOWDOB,
			Messages.UserSettings2_Birthdate, getFieldEditorParent()));
		addField(new ComboFieldEditor(Preferences.USR_PATLIST_FOCUSFIELD, "Fokusfeld",
			patlistFocusFields, getFieldEditorParent()));
		new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(SWTHelper
			.getFillGridData(2, true, 1, false));
		new Label(getFieldEditorParent(), SWT.NONE)
			.setText(Messages.UserSettings2_AddidtionalFields);
		addField(new MultilineFieldEditor(Patientenblatt2.CFG_EXTRAFIELDS, StringTool.leer, 5,
			SWT.NONE, true, getFieldEditorParent()));
		
	}
	
	public void init(IWorkbench workbench){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean performOk(){
		prefs.flush();
		CoreHub.userCfg.flush();
		SWTHelper.reloadViewPart(UiResourceConstants.PatientenListeView_ID);
		return super.performOk();
	}
}
