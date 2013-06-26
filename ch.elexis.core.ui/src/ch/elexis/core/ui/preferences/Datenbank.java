/*******************************************************************************
 * Copyright (c) 2005-2009, G. Weirich and Elexis
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

import java.io.File;
import java.io.FileOutputStream;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.util.DatabaseCleaner;
import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.io.Settings;
import ch.rgw.tools.ExHandler;

/**
 * Datenbankspezifische Einstellungen. Datenbanktyp, Connect-String, Jdbc-Klasse usw.
 */
public class Datenbank extends PreferencePage implements IWorkbenchPreferencePage {
	
	Button bKons, bRn, bRepair;
	Label lOutputFile;
	Button bOutputFile, bCheck;
	Settings cfg;
	
	public Datenbank(){
		
		noDefaultAndApplyButton();
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.localCfg));
		cfg = CoreHub.localCfg;
		setDescription(Messages.Datenbank_databaseConnectionHeading);
	}
	
	@Override
	protected Control createContents(Composite parent){
		final Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new GridLayout(2, false));
		new Label(ret, SWT.NONE).setText(Messages.Datenbank_databaseConnection);
		new Text(ret, SWT.READ_ONLY).setText(cfg.get(Preferences.DB_CLASS, "")); //$NON-NLS-1$
		new Label(ret, SWT.NONE).setText(Messages.Datenbank_connectString);
		new Text(ret, SWT.READ_ONLY).setText(cfg.get(Preferences.DB_CONNECT, "")); //$NON-NLS-1$
		new Label(ret, SWT.NONE).setText(Messages.Datenbank_usernameForDatabase);
		new Text(ret, SWT.READ_ONLY).setText(cfg.get(Preferences.DB_USERNAME, "")); //$NON-NLS-1$
		new Label(ret, SWT.NONE).setText(Messages.Datenbank_passwordForDatabase);
		new Text(ret, SWT.READ_ONLY).setText(cfg.get(Preferences.DB_PWD, "")); //$NON-NLS-1$
		new Label(ret, SWT.NONE).setText(Messages.Datenbank_typeOfDatabase);
		new Text(ret, SWT.READ_ONLY).setText(cfg.get(Preferences.DB_TYP, "")); //$NON-NLS-1$
		
		new Label(ret, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(SWTHelper.getFillGridData(2,
			true, 1, false));
		if (false) { // TODO
			new Label(ret, SWT.NONE).setText(Messages.Datenbank_reorganization);
			bRepair = new Button(ret, SWT.CHECK);
			bRepair.setText(Messages.Datenbank_repairImmediately);
			bOutputFile = new Button(ret, SWT.PUSH);
			bOutputFile.setText(Messages.Datenbank_writeLogTo);
			bOutputFile.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e){
					FileDialog fd = new FileDialog(ret.getShell(), SWT.SAVE);
					String f = fd.open();
					if (f != null) {
						lOutputFile.setText(f);
					}
				}
			});
			lOutputFile = new Label(ret, SWT.NONE);
			lOutputFile.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			bKons = new Button(ret, SWT.CHECK);
			bKons.setText(Messages.Datenbank_checkKonsultations);
			bRn = new Button(ret, SWT.CHECK);
			bRn.setText(Messages.Datenbank_checkBills);
			bCheck = new Button(ret, SWT.PUSH);
			bCheck.setText(Messages.Datenbank_doCheck);
			bCheck.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e){
					try {
						File fo = new File(lOutputFile.getText());
						fo.createNewFile();
						FileOutputStream fos = new FileOutputStream(fo);
						DatabaseCleaner dc = new DatabaseCleaner(fos, bRepair.getSelection());
						if (bKons.getSelection()) {
							dc.checkKonsultationen();
						}
						if (bRn.getSelection()) {
							dc.checkRechnungen();
						}
						fos.close();
					} catch (Exception ex) {
						ExHandler.handle(ex);
						MessageDialog.openError(getShell(), Messages.Datenbank_errorWritingLog,
							Messages.Datenbank_couldntCreateLog);
					}
				}
			});
		} // false
		return ret;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench){/* leer */
	}
	
}