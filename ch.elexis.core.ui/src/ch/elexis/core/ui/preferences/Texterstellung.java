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
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.services.ILocalDocumentService;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.services.LocalDocumentServiceHolder;
import ch.elexis.core.ui.util.SWTHelper;

/**
 * Einstellungen zur Verknüpfung mit einem externen Texterstellungs-Modul
 * 
 * @author Gerry
 */
public class Texterstellung extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
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
		
		if (LocalDocumentServiceHolder.getService().isPresent())
		{
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
		
	}
	
	public void init(IWorkbench workbench){}
	
}
