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
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.preferences.Messages;
import ch.elexis.core.ui.preferences.SettingsPreferenceStore;
import ch.elexis.core.ui.preferences.inputs.PrefAccessDenied;

public class LabSettings extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private static final Logger log = LoggerFactory.getLogger(LabSettings.class);
	
	private Text txtKeepUnseen;
	private String daysKeepUnseen;
	
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
		addField(new BooleanFieldEditor(Preferences.LABSETTINGS_CFG_SHOW_MANDANT_ORDERS_ONLY,
			Messages.LabSettings_showOrdersActiveMandant, getFieldEditorParent()));
			
		addField(new RadioGroupFieldEditor(Preferences.LABSETTINGS_CFG_LABNEW_HEARTRATE,
			Messages.LabSettings_frequencyNewLabvalues, 3, new String[][] {
				{
					Messages.LabSettings_normal, "1" //$NON-NLS-1$
				}, {
					Messages.LabSettings_medium, "2" //$NON-NLS-1$
				}, {
					Messages.LabSettings_slow, "3" //$NON-NLS-1$
				}
		}, getFieldEditorParent()));
		
		addField(new BooleanFieldEditor(Preferences.LABSETTINGS_CFG_LOCAL_REFVALUES,
			Messages.LabSettings_useLocalLabRefValues, getFieldEditorParent()));
			
		Composite area = new Composite(getFieldEditorParent(), SWT.NONE);
		area.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		area.setLayout(new GridLayout(2, false));
		Label lblKeepUnseen = new Label(area, SWT.NONE);
		lblKeepUnseen.setText(Messages.LabSettings_showNewLabvaluesDays);
		txtKeepUnseen = new Text(area, SWT.BORDER);
		txtKeepUnseen.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		txtKeepUnseen.setText(daysKeepUnseen);
	}
	
	public void init(final IWorkbench workbench){
		daysKeepUnseen =
			CoreHub.globalCfg.get(Preferences.LABSETTINGS_CFG_KEEP_UNSEEN_LAB_RESULTS, null);
		if (daysKeepUnseen == null || !isValidNumber(daysKeepUnseen)) {
			CoreHub.globalCfg.set(Preferences.LABSETTINGS_CFG_KEEP_UNSEEN_LAB_RESULTS,
				Preferences.DAYS_TO_KEEP_UNSEEN_LAB_RESULTS);
			daysKeepUnseen = Preferences.DAYS_TO_KEEP_UNSEEN_LAB_RESULTS;
		}
	}
	
	@Override
	public boolean performOk(){
		if (isValidNumber(txtKeepUnseen.getText())) {
			CoreHub.globalCfg.set(Preferences.LABSETTINGS_CFG_KEEP_UNSEEN_LAB_RESULTS,
				txtKeepUnseen.getText());
			CoreHub.globalCfg.flush();
		}
		CoreHub.userCfg.flush();
		return super.performOk();
	}
	
	private boolean isValidNumber(String nrString){
		try {
			Integer.parseInt(nrString);
			return true;
		} catch (NumberFormatException nfe) {
			log.warn("Can't use [" + nrString
				+ "] for KeepUnseen in lab settings as it can't be parsed to an integer.");
			return false;
		}
	}
}
