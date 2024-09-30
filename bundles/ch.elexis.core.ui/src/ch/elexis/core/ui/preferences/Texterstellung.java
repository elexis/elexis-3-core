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
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.preferences.PreferencesUtil;
import ch.elexis.core.services.ILocalDocumentService;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.e4.jface.preference.URIFieldEditor;
import ch.elexis.core.ui.preferences.ConfigServicePreferenceStore.Scope;
import ch.elexis.core.ui.services.LocalDocumentServiceHolder;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.utils.CoreUtil;
import ch.elexis.core.utils.CoreUtil.OS;
import ch.elexis.data.Brief;

/**
 * Einstellungen zur Verknüpfung mit einem externen Texterstellungs-Modul
 *
 * @author Gerry
 */
public class Texterstellung extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private ControlDecoration externPathDeco;
	private Label allExtern;

	public Texterstellung() {
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.localCfg));
		setDescription(Messages.Texterstellung_TextProcessor);
	}

	@Override
	protected void createFieldEditors() {

		List<IConfigurationElement> list = Extensions.getExtensions(ExtensionPointConstantsUi.TEXTPROCESSINGPLUGIN);
		addField(new BooleanFieldEditor(Preferences.P_TEXT_SUPPORT_LEGACY, Messages.Texterstellung_Support_Legacy,
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(Preferences.P_TEXT_RENAME_WITH_F2, Messages.Texterstellung_Rename_with_F2,
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(Preferences.P_TEXT_EDIT_LOCAL, Messages.Texterstellung_texteditlocaldesc,
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(Preferences.P_TEXT_DIAGNOSE_EXPORT_WORD_FORMAT,
				Messages.Texterstellung_DiagnoseExportAlternativeFormat,
				getFieldEditorParent()));

		if (LocalDocumentServiceHolder.getService().isPresent()) {
			ILocalDocumentService documentService = LocalDocumentServiceHolder.getService().get();
			Composite compBackupDir = new Composite(getFieldEditorParent(), SWT.NONE);
			compBackupDir.setLayout(new GridLayout(2, false));
			compBackupDir.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			new Label(compBackupDir, SWT.NONE).setText(Messages.Texterstellung_backupdir);
			Text backupDir = new Text(compBackupDir, SWT.BORDER);
			backupDir.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			backupDir.setText(documentService.getDocumentCachePath() + File.separator + "backup"); //$NON-NLS-1$
			backupDir.setEditable(false);
			Button restore = new Button(compBackupDir, SWT.PUSH);
			restore.setText("wiederherstellen");
			restore.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					restoreLocalDocuments(documentService);
				}
			});
		}

		String[][] rows = new String[list.size()][];
		int i = 0;
		for (IConfigurationElement ice : list) {
			rows[i] = new String[2];
			rows[i][1] = ice.getAttribute("name"); //$NON-NLS-1$
			rows[i][0] = Integer.toString(i) + " : " + rows[i][1]; //$NON-NLS-1$
			i += 1;
		}
		addField(new RadioGroupFieldEditor(Preferences.P_TEXTMODUL, Messages.Texterstellung_ExternalProgram, 2,
				/*
				 * new String[][] { { "&0: Keines", "none" }, { "&1: OpenOffice", "OpenOffice" }
				 */
				rows, getFieldEditorParent()));

		Composite compExtern = new Composite(getFieldEditorParent(), SWT.NONE);
		compExtern.setLayout(new GridLayout(2, false));
		compExtern.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));

		Button check = new Button(compExtern, SWT.CHECK);
		check.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		check.setText(Messages.Texterstellung_external_save);
		check.setSelection(ConfigServiceHolder.getGlobal(Preferences.P_TEXT_EXTERN_FILE, false));
		check.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ConfigServiceHolder.setGlobal(Preferences.P_TEXT_EXTERN_FILE, check.getSelection());
				allExtern.setEnabled(check.getSelection());
				if (externPathDeco != null) {
					externPathDeco.hide();
				}
			}
		});

		Composite comp = new Composite(compExtern, SWT.None);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		Combo comboOs = new Combo(comp, SWT.None);
		ComboViewer cvOs = new ComboViewer(comboOs);
		comboOs.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		cvOs.setContentProvider(ArrayContentProvider.getInstance());
		cvOs.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((CoreUtil.OS) element).name();
			}
		});
		cvOs.setInput(CoreUtil.OS.values());

		URIFieldEditor storePath = new URIFieldEditor(Preferences.P_TEXT_EXTERN_FILE_PATH, StringUtils.EMPTY, comp);
		storePath.setPreferenceStore(new ConfigServicePreferenceStore(Scope.GLOBAL));
		storePath.setEmptyStringAllowed(true);
		addField(storePath);

		cvOs.addSelectionChangedListener(event -> {
			CoreUtil.OS selection = (OS) event.getStructuredSelection().getFirstElement();
			storePath.store();
			storePath.setPreferenceName(PreferencesUtil.getOsSpecificPreferenceName(selection, Preferences.P_TEXT_EXTERN_FILE_PATH));
			storePath.load();
		});

		allExtern = new Label(compExtern, SWT.WRAP);
		allExtern.setText(Messages.Texterstellung_save_all_letters_externally);
		allExtern.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		cvOs.setSelection(new StructuredSelection(CoreUtil.getOperatingSystemType()));
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	private void restoreLocalDocuments(ILocalDocumentService documentService) {
		File backupDir = new File(documentService.getDocumentCachePath() + File.separator + "backup"); //$NON-NLS-1$
		if (backupDir.exists()) {
			Map<File, Brief> existing = new HashMap<>();
			for (File file : backupDir.listFiles()) {
				String id = getBriefId(file);
				if (StringUtils.isNotBlank(id)) {
					Brief loaded = Brief.load(id);
					if (loaded.exists()) {
						existing.put(file, loaded);
					}
				}
			}
			if (existing.isEmpty()) {
				MessageDialog.openInformation(getShell(), "Briefe wiederherstellen",
						"Es wurden keine wiederherstellbaren Briefe gefunden");
			} else {
				Date fileFrom = new Date(existing.keySet().stream().mapToLong(f -> f.lastModified()).min().getAsLong());
				Date fileTo = new Date(existing.keySet().stream().mapToLong(f -> f.lastModified()).max().getAsLong());
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy"); //$NON-NLS-1$
				if (MessageDialog.openQuestion(getShell(), "Briefe wiederherstellen",
						"Sollen die " + existing.size() + " Briefe aus Datei(en) vom " + dateFormat.format(fileFrom)
								+ " bis " + dateFormat.format(fileTo) + " wiederhergestellt werden?")) {
					for (File backupFile : existing.keySet()) {
						Brief brief = existing.get(backupFile);
						try (FileInputStream fin = new FileInputStream(backupFile)) {
							byte[] contentToStore = new byte[(int) backupFile.length()];
							fin.read(contentToStore);
							brief.save(contentToStore, FilenameUtils.getExtension(backupFile.getName()));
							backupFile.delete();
						} catch (IOException e) {
							LoggerFactory.getLogger(getClass()).error("Error restoring local backup", e); //$NON-NLS-1$
						}
					}
				}
			}
		}
	}

	private String getBriefId(File file) {
		String filename = file.getName();
		int startIndex = filename.lastIndexOf('[');
		int endIndex = filename.lastIndexOf(']');
		if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
			return filename.substring(startIndex + 1, endIndex);
		}
		return null;
	}
}
