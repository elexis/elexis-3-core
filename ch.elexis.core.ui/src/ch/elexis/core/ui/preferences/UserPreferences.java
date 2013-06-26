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

package ch.elexis.core.ui.preferences;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.data.NamedBlob;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.ShutdownJob;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.io.FileTool;
import ch.rgw.io.InMemorySettings;
import ch.rgw.io.Settings;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

public class UserPreferences extends PreferencePage implements IWorkbenchPreferencePage {
	
	Button bLoad, bSave, bWorkspaceLoad, bWorkspaceSave;
	// Text tLoad, tSave, tWorkspaceLoad, tWorkspaceSave;
	Combo cbUserSave, cbWSSave, cbUserLoad, cbWSLoad;
	String[] userPrefs;
	String[] WSPrefs;
	
	public UserPreferences(){
		noDefaultAndApplyButton();
	}
	
	@Override
	protected Control createContents(Composite parent){
		final String layoutfile =
			Platform.getInstanceLocation().getURL().getPath() + File.separator + ".metadata" //$NON-NLS-1$
				+ File.separator + ".plugins" //$NON-NLS-1$
				+ File.separator + "org.eclipse.ui.workbench" //$NON-NLS-1$
				+ File.separator + "workbench.xml"; //$NON-NLS-1$
		Composite ret = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout(2, false);
		gl.verticalSpacing = 10;
		ret.setLayout(gl);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		Label desc = new Label(ret, SWT.WRAP);
		desc.setText(Messages.UserPreferences_Explanation1 + Messages.UserPreferences_Explanation2
			+ Messages.UserPreferences_Explanation3);
		desc.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		List<NamedBlob> userBlobs = NamedBlob.findFromPrefix("UserCfg:"); //$NON-NLS-1$
		userPrefs = new String[userBlobs.size()];
		for (int i = 0; i < userPrefs.length; i++) {
			userPrefs[i] = userBlobs.get(i).getId().split(":")[1]; //$NON-NLS-1$
		}
		List<NamedBlob> wsBlobs = NamedBlob.findFromPrefix("Workspace:"); //$NON-NLS-1$
		WSPrefs = new String[wsBlobs.size()];
		for (int i = 0; i < WSPrefs.length; i++) {
			WSPrefs[i] = wsBlobs.get(i).getId().split(":")[1]; //$NON-NLS-1$
		}
		bLoad = new Button(ret, SWT.PUSH);
		bLoad.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		bLoad.setText(Messages.UserPreferences_LoadSettingsfrom);
		bLoad.setLayoutData(new GridData(GridData.FILL));
		bLoad.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0){
				String name = cbUserLoad.getText();
				if (StringTool.isNothing(name)) {
					SWTHelper.showInfo(Messages.UserPreferences_NoNameGiven,
						Messages.UserPreferences_PleaseEnterName);
				} else if (NamedBlob.exists(Messages.UserPreferences_14 + name)) {
					NamedBlob blob = NamedBlob.load("UserCfg:" + name); //$NON-NLS-1$
					InMemorySettings ims = new InMemorySettings(blob.getHashtable());
					CoreHub.userCfg.overlay(ims, Settings.OVL_REPLACE);
				} else {
					SWTHelper.showError(Messages.UserPreferences_KonfigNotFound,
						MessageFormat.format(Messages.UserPreferences_ConfigWasNotFound, name));
				}
			}
			
		});
		cbUserLoad = new Combo(ret, SWT.READ_ONLY | SWT.SINGLE);
		cbUserLoad.setItems(userPrefs);
		cbUserLoad.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		bSave = new Button(ret, SWT.PUSH);
		bSave.setText(Messages.UserPreferences_SaveSettingsTo);
		bSave.setLayoutData(new GridData(GridData.FILL));
		bSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0){
				String name = cbUserSave.getText();
				if (StringTool.isNothing(name)) {
					SWTHelper.showInfo(Messages.UserPreferences_NoNameGiven,
						Messages.UserPreferences_PleaseEnterName2);
				} else {
					NamedBlob blob = NamedBlob.load("UserCfg:" + name); //$NON-NLS-1$
					InMemorySettings ims = new InMemorySettings();
					ims.overlay(CoreHub.userCfg, Settings.OVL_REPLACE);
					blob.put(ims.getNode());
					SWTHelper.showInfo(Messages.UserPreferences_ConfigSaved,
						MessageFormat.format(Messages.UserPreferences_ConfigWasSaved, name));
					cbUserSave.setText(""); //$NON-NLS-1$
				}
			}
		});
		cbUserSave = new Combo(ret, SWT.SINGLE);
		cbUserSave.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		cbUserSave.setItems(userPrefs);
		bWorkspaceLoad = new Button(ret, SWT.PUSH);
		bWorkspaceLoad.setText(Messages.UserPreferences_LoadDeskSettingsFrom);
		bWorkspaceLoad.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0){
				String name = cbWSLoad.getText();
				
				if (StringTool.isNothing(name)) {
					SWTHelper.showInfo(Messages.UserPreferences_NoNameGiven,
						Messages.UserPreferences_PleaseEnterName3);
				} else if (NamedBlob.exists("Workspace:" + name)) { //$NON-NLS-1$
					NamedBlob blob = NamedBlob.load("Workspace:" + name); //$NON-NLS-1$
					InMemorySettings ims = new InMemorySettings(blob.getHashtable());
					final String newloc = ims.get("perspectivelayout", null); //$NON-NLS-1$
					if (newloc != null) {
						ShutdownJob job = new ShutdownJob() {
							
							public void doit(){
								try {
									File file = new File(layoutfile);
									FileTool.copyFile(file, new File(layoutfile + ".bak"), //$NON-NLS-1$
										FileTool.REPLACE_IF_EXISTS);
									file.delete();
									FileWriter fout = new FileWriter(file);
									fout.write(newloc);
									fout.close();
								} catch (Exception ex) {
									ExHandler.handle(ex);
								}
								
							}
						};
						Hub.addShutdownJob(job);
						SWTHelper.showInfo(Messages.UserPreferences_ConfigLoaded,
							Messages.UserPreferences_ConfigActiveNextTime);
					}
				} else {
					SWTHelper.showError(Messages.UserPreferences_ConfigNotFound,
						Messages.UserPreferences_3 + name + Messages.UserPreferences_4);
				}
			}
			
		});
		bWorkspaceLoad.setLayoutData(new GridData(GridData.FILL));
		cbWSLoad = new Combo(ret, SWT.SINGLE | SWT.READ_ONLY);
		cbWSLoad.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		cbWSLoad.setItems(WSPrefs);
		bWorkspaceSave = new Button(ret, SWT.PUSH);
		bWorkspaceSave.setText(Messages.UserPreferences_WorkspaceSettingsSaveTo);
		bWorkspaceSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0){
				String name = cbWSSave.getText();
				
				if (StringTool.isNothing(name)) {
					SWTHelper.showInfo(Messages.UserPreferences_NoNameGiven,
						Messages.UserPreferences_PleaseEnterName4);
				} else {
					try {
						File file = new File(layoutfile);
						FileReader reader = new FileReader(file);
						StringBuilder sb = new StringBuilder(1000);
						char[] load = new char[4096];
						while (true) {
							int x = reader.read(load);
							if (x == -1) {
								break;
							}
							sb.append(load, 0, x);
						}
						reader.close();
						NamedBlob blob = NamedBlob.load("Workspace:" + name); //$NON-NLS-1$
						InMemorySettings ims = new InMemorySettings();
						ims.set("perspectivelayout", sb.toString()); //$NON-NLS-1$
						blob.put(ims.getNode());
					} catch (Exception ex) {
						ExHandler.handle(ex);
					}
					
				}
			}
			
		});
		bWorkspaceSave.setLayoutData(new GridData(GridData.FILL));
		cbWSSave = new Combo(ret, SWT.SINGLE);
		cbWSSave.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		cbWSSave.setItems(WSPrefs);
		return ret;
	}
	
	public void init(IWorkbench workbench){
		// TODO Auto-generated method stub
		
	}
	
}