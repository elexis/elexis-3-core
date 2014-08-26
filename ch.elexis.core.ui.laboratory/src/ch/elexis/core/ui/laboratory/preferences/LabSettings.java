/*******************************************************************************
 * Copyright (c) 2007-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.laboratory.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.preferences.Messages;
import ch.elexis.core.ui.preferences.SettingsPreferenceStore;
import ch.elexis.core.ui.preferences.inputs.PrefAccessDenied;

public class LabSettings extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public static final String KEEP_UNSEEN_LAB_RESULTS = "lab/keepUnseen"; //$NON-NLS-1$
	public static final String LABNEW_HEARTRATE = "lab/heartrate_unseen"; //$NON-NLS-1$
	
	public static final String LABORDERS_SHOWMANDANTONLY = "lab/showMandantOnly"; //$NON-NLS-1$
	
	public LabSettings(){
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.userCfg));
	}
	
	@Override
	protected Control createContents(final Composite parent){
		if (CoreHub.acl.request(AccessControlDefaults.LAB_SEEN)) {
			return super.createContents(parent);
		} else {
			return new PrefAccessDenied(parent);
		}
	}
	
	@Override
	protected void createFieldEditors(){
		addField(new BooleanFieldEditor(LABORDERS_SHOWMANDANTONLY,
			Messages.LabSettings_showOrdersActiveMandant, getFieldEditorParent()));
		
		addField(new StringFieldEditor(KEEP_UNSEEN_LAB_RESULTS,
			Messages.LabSettings_showNewLabvaluesDays, getFieldEditorParent()));
		addField(new RadioGroupFieldEditor(
			LABNEW_HEARTRATE,
			Messages.LabSettings_frequencyNewLabvalues,
			3,
			new String[][] {
				{
					Messages.LabSettings_normal, "1"}, { Messages.LabSettings_medium, "2"}, { Messages.LabSettings_slow, "3"}}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			getFieldEditorParent()));
		
	}
	
	public void init(final IWorkbench workbench){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean performOk(){
		CoreHub.userCfg.flush();
		return super.performOk();
	}
	
}
