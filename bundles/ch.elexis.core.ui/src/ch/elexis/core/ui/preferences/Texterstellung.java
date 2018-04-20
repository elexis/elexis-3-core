/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
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
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.util.BriefExternUtil;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.services.ILocalDocumentService;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.services.LocalDocumentServiceHolder;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Brief;
import ch.elexis.data.Query;

/**
 * Einstellungen zur Verknüpfung mit einem externen Texterstellungs-Modul
 * 
 * @author Gerry
 */
public class Texterstellung extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	private Text externPath;
	private ControlDecoration externPathDeco;
	private Button allExtern;
	
	public Texterstellung(){
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.localCfg));
		setDescription(Messages.Texterstellung_TextProcessor);
	}
	
	@Override
	protected void createFieldEditors(){
		
		List<IConfigurationElement> list =
			Extensions.getExtensions(ExtensionPointConstantsUi.TEXTPROCESSINGPLUGIN);
		addField(new BooleanFieldEditor(Preferences.P_TEXT_SUPPORT_LEGACY,
				Messages.Texterstellung_Support_Legacy, getFieldEditorParent()));
		addField(new BooleanFieldEditor(Preferences.P_TEXT_RENAME_WITH_F2,
				Messages.Texterstellung_Rename_with_F2, getFieldEditorParent()));
		
		addField(new BooleanFieldEditor(Preferences.P_TEXT_EDIT_LOCAL,
			Messages.Texterstellung_texteditlocaldesc,
			getFieldEditorParent()));
		
		if (LocalDocumentServiceHolder.getService().isPresent()) {
			ILocalDocumentService documentService = LocalDocumentServiceHolder.getService().get();
			Composite compBackupDir = new Composite(getFieldEditorParent(), SWT.NONE);
			compBackupDir.setLayout(new GridLayout(1, false));
			compBackupDir.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			new Label(compBackupDir, SWT.NONE).setText(Messages.Texterstellung_backupdir);
			Text backupDir = new Text(compBackupDir, SWT.BORDER);
			backupDir.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			backupDir.setText(documentService.getDocumentCachePath() + File.separator + "backup");
			backupDir.setEditable(false);
		}
		
				
		String[][] rows = new String[list.size()][];
		int i = 0;
		for (IConfigurationElement ice : list) {
			rows[i] = new String[2];
			rows[i][1] = ice.getAttribute("name"); //$NON-NLS-1$
			rows[i][0] = Integer.toString(i) + " : " + rows[i][1]; //$NON-NLS-1$
			i += 1;
		}
		addField(new RadioGroupFieldEditor(Preferences.P_TEXTMODUL,
			Messages.Texterstellung_ExternalProgram, 2,
			/*
			 * new String[][] { { "&0: Keines", "none" }, { "&1: OpenOffice", "OpenOffice" }
			 */
			rows, getFieldEditorParent()));
		
		Composite compExtern = new Composite(getFieldEditorParent(), SWT.NONE);
		compExtern.setLayout(new GridLayout(2, false));
		compExtern.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		Button check = new Button(compExtern, SWT.CHECK);
		check.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		check.setText("Brief extern speichern (gleicher UNC Pfad auf allen Stationen)");
		check.setSelection(CoreHub.globalCfg.get(Preferences.P_TEXT_EXTERN_FILE, false));
		check.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				CoreHub.globalCfg.set(Preferences.P_TEXT_EXTERN_FILE, check.getSelection());
				externPath.setEnabled(check.getSelection());
				allExtern.setEnabled(check.getSelection());
				externPathDeco.hide();
			}
		});
		externPath = new Text(compExtern, SWT.BORDER);
		externPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		externPath.setText(CoreHub.globalCfg.get(Preferences.P_TEXT_EXTERN_FILE_PATH, ""));
		externPath.setEnabled(check.getSelection());
		externPath.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e){
				updateExternPathDeco(externPath.getText());
			}
		});
		externPathDeco = new ControlDecoration(externPath, SWT.LEFT | SWT.TOP);
		
		allExtern = new Button(compExtern, SWT.PUSH);
		allExtern.setText("Alle Brief extern speichern");
		allExtern.setEnabled(check.getSelection());
		allExtern.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent ev){
				if (MessageDialog.openQuestion(getShell(), "Extern speichern",
					"Wollen sie wirklich alle Briefe extern speichern, und aus der Datenbank entfernen?")) {
					ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(getShell());
					try {
						progressDialog.run(true, true, new IRunnableWithProgress() {
							public void run(IProgressMonitor monitor)
								throws InvocationTargetException, InterruptedException{
								Query<Brief> query = new Query<>(Brief.class);
								List<Brief> allBrief = query.execute();
								monitor.beginTask("Alle Briefe extern speichern", allBrief.size());
								for (Brief brief : allBrief) {
									BriefExternUtil.exportToExtern(brief);
									monitor.worked(1);
								}
							}
						});
					} catch (InvocationTargetException | InterruptedException e) {
						MessageDialog.openError(getShell(), "Extern speichern",
							"Fehler beim Briefe extern speichern.");
						LoggerFactory.getLogger(getClass())
							.error("Error creating saving Brief extern", e);
					}
				}
			}
		});
		if (check.getSelection()) {
			updateExternPathDeco(externPath.getText());
		}
	}
	
	private void updateExternPathDeco(String path){
		if (BriefExternUtil.isValidExternPath(path, false)) {
			externPathDeco.hide();
			allExtern.setEnabled(true);
		} else {
			externPathDeco.setImage(FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage());
			externPathDeco.setDescriptionText(getPathDiagnoseString(path));
			externPathDeco.show();
			allExtern.setEnabled(false);
		}
	}
	
	private String getPathDiagnoseString(String path){
		if (path == null) {
			return "Kein Pfad gesetzt.";
		} else {
			File dir = new File(path);
			if (!dir.exists()) {
				return "Pfad existiert nicht, bzw. ist nicht erreichbar.";
			}
			if (!dir.isDirectory()) {
				return "Pfad ist keine Verzeichnis.";
			}
			if (!dir.canWrite()) {
				return "Keine Schreibberechtigung auf Verzeichnis";
			}
		}
		return "?";
	}
	
	@Override
	public boolean performOk(){
		if (externPath != null && !externPath.isDisposed()
			&& BriefExternUtil.isValidExternPath(externPath.getText(), false)) {
			CoreHub.globalCfg.set(Preferences.P_TEXT_EXTERN_FILE_PATH, externPath.getText());
			CoreHub.globalCfg.flush();
		}
		return super.performOk();
	}
	
	public void init(IWorkbench workbench){}
	
}
