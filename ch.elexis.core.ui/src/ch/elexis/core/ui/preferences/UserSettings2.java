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

package ch.elexis.core.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.preferences.inputs.MultilineFieldEditor;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.PatientenListeView;
import ch.elexis.core.ui.views.Patientenblatt2;
import ch.rgw.tools.StringTool;

public class UserSettings2 extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public static final String REMEMBER_LAST_STATE = Messages.UserSettings2_RememberLastState;
	public static final String ALWAYS_CLOSED = Messages.UserSettings2_AlwaysClosed;
	public static final String ALWAYS_OPEN = Messages.UserSettings2_AlwaysOpen;
	public static final String EXPANDABLE_COMPOSITES_BASE = "view/expandableComposites"; //$NON-NLS-1$
	public static final String EXPANDABLE_COMPOSITES = EXPANDABLE_COMPOSITES_BASE + "/setting"; //$NON-NLS-1$
	public static final String STATES = EXPANDABLE_COMPOSITES_BASE + "/states/"; //$NON-NLS-1$
	public static final String OPEN = "1"; //$NON-NLS-1$
	public static final String CLOSED = "2"; //$NON-NLS-1$
	public static final String REMEMBER_STATE = "3"; //$NON-NLS-1$
	
	private SettingsPreferenceStore prefs = new SettingsPreferenceStore(CoreHub.userCfg);
	
	public UserSettings2(){
		super(GRID);
		setPreferenceStore(prefs);
		prefs.setDefault(EXPANDABLE_COMPOSITES, REMEMBER_STATE);
		prefs.setDefault(Preferences.USR_PATLIST_SHOWPATNR, false);
		prefs.setDefault(Preferences.USR_PATLIST_SHOWNAME, true);
		prefs.setDefault(Preferences.USR_PATLIST_SHOWFIRSTNAME, true);
		prefs.setDefault(Preferences.USR_PATLIST_SHOWDOB, true);
	}
	
	@Override
	protected void createFieldEditors(){
		addField(new RadioGroupFieldEditor(EXPANDABLE_COMPOSITES,
			Messages.UserSettings2_ExtendableFields, 1, new String[][] {
				{
					ALWAYS_OPEN, OPEN
				}, {
					ALWAYS_CLOSED, CLOSED
				}, {
					REMEMBER_LAST_STATE, REMEMBER_STATE
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
		SWTHelper.reloadViewPart(PatientenListeView.ID);
		return super.performOk();
	}
	
	/**
	 * save the state of an expandable composite
	 * 
	 * @param field
	 *            name of the composite (any unique string, preferably derived from view name)
	 * @param state
	 *            the state to save
	 */
	public static void saveExpandedState(final String field, final boolean state){
		if (state) {
			CoreHub.userCfg.set(UserSettings2.STATES + field, UserSettings2.OPEN);
		} else {
			CoreHub.userCfg.set(UserSettings2.STATES + field, UserSettings2.CLOSED);
		}
	}
	
	/**
	 * Set the state of an expandable Composite to the previously saved state.
	 * 
	 * @param ec
	 *            the expandable Composite to expand or collapse
	 * @param field
	 *            the unique name
	 */
	public static void setExpandedState(final ExpandableComposite ec, final String field){
		String mode =
			CoreHub.userCfg.get(UserSettings2.EXPANDABLE_COMPOSITES, UserSettings2.REMEMBER_STATE);
		if (mode.equals(UserSettings2.OPEN)) {
			ec.setExpanded(true);
		} else if (mode.equals(UserSettings2.CLOSED)) {
			ec.setExpanded(false);
		} else {
			String state = CoreHub.userCfg.get(UserSettings2.STATES + field, UserSettings2.CLOSED);
			if (state.equals(UserSettings2.CLOSED)) {
				ec.setExpanded(false);
			} else {
				ec.setExpanded(true);
			}
		}
	}
}
