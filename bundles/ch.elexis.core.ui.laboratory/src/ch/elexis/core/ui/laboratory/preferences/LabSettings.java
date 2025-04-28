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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.laboratory.controls.util.HL7AutoGroupImporter;
import ch.elexis.core.ui.preferences.ConfigServicePreferenceStore;
import ch.elexis.core.ui.preferences.ConfigServicePreferenceStore.Scope;
import ch.elexis.data.LabMapping;
import ch.elexis.data.Query;

public class LabSettings extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private static final Logger log = LoggerFactory.getLogger(LabSettings.class);

	private Text txtKeepUnseen;
	private String daysKeepUnseen;
	private Text txtAnzahlMonate;
	private Font histogramFont;

	public LabSettings() {
		super(GRID);
		setPreferenceStore(new ConfigServicePreferenceStore(Scope.USER));
		// make sure default value is correct, in case no value is set yet
		getPreferenceStore().setDefault(Preferences.LABSETTINGS_CFG_LOCAL_REFVALUES, true);
	}

	@Override
	protected Control createContents(final Composite parent) {
			return super.createContents(parent);
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(Preferences.LABSETTINGS_CFG_SHOW_MANDANT_ORDERS_ONLY,
				Messages.LabSettings_showOrdersActiveMandant, getFieldEditorParent()));

		addField(new RadioGroupFieldEditor(Preferences.LABSETTINGS_CFG_LABNEW_HEARTRATE,
				Messages.LabSettings_frequencyNewLabvalues, 3, new String[][] { { Messages.Core_Normal, "1" //$NON-NLS-1$
				}, { Messages.LabSettings_medium, "2" //$NON-NLS-1$
				}, { Messages.LabSettings_slow, "3" //$NON-NLS-1$
				} }, getFieldEditorParent()));

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

		Button btnValidateMappings = new Button(area, SWT.PUSH);
		btnValidateMappings.setText(Messages.LabSettings_validateMappings);
		btnValidateMappings.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Query<LabMapping> query = new Query<>(LabMapping.class);
				query.add(LabMapping.FLD_ID, Query.NOT_EQUAL, LabMapping.VERSIONID); // $NON-NLS-1$
				List<LabMapping> mappings = query.execute();
				int countDeleted = 0;
				for (LabMapping labMapping : mappings) {
					if (!labMapping.isMappingValid()) {
						countDeleted++;
						labMapping.delete();
					}
				}
				MessageDialog.openInformation(getShell(), Messages.LabSettings_validateMappings,
						MessageFormat.format(Messages.LabSettings_validateMappingsResult, countDeleted));
			}
		});
		Label lblHistogramTitle = new Label(getFieldEditorParent(), SWT.NONE);
		lblHistogramTitle.setText(Messages.LabSettings_histogramTitle);
		FontData[] fontData = lblHistogramTitle.getFont().getFontData();
		for (FontData fd : fontData) {
			fd.setHeight(10);
		}
		histogramFont = new Font(getFieldEditorParent().getDisplay(), fontData);
		lblHistogramTitle.setFont(histogramFont);
		GridData histogramTitleGridData = new GridData(SWT.FILL, SWT.TOP, true, false);
		histogramTitleGridData.verticalIndent = 20;
		lblHistogramTitle.setLayoutData(histogramTitleGridData);
		Label separator = new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		Composite histogramComposite = new Composite(getFieldEditorParent(), SWT.NONE);
		histogramComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		histogramComposite.setLayout(new GridLayout(1, false));
		BooleanFieldEditor histogramPopupCheckbox = new BooleanFieldEditor(Preferences.LABSETTINGS_HISTOGRAM_POPUP,
				Messages.LabSettings_histogramPopupLabel, histogramComposite);
		addField(histogramPopupCheckbox);
		Composite monthsComposite = new Composite(histogramComposite, SWT.NONE);
		monthsComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		monthsComposite.setLayout(new GridLayout(2, false));
		Label lblAnzahlMonate = new Label(monthsComposite, SWT.NONE);
		lblAnzahlMonate
				.setText(Messages.Core_Count + StringUtils.SPACE + Messages.AbstractGraphicalView_monthActionLabel);
		txtAnzahlMonate = new Text(monthsComposite, SWT.BORDER);
		txtAnzahlMonate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		txtAnzahlMonate.setText(ConfigServiceHolder.getUser(Preferences.LABSETTINGS_ANZAHL_MONATE, "12")); //$NON-NLS-1$
		txtAnzahlMonate.addModifyListener(event -> {
			ConfigServiceHolder.setUser(Preferences.LABSETTINGS_ANZAHL_MONATE, txtAnzahlMonate.getText());
		});

		Label lblImportTitle = new Label(getFieldEditorParent(), SWT.NONE);
		lblImportTitle.setText(Messages.LabSettings_importTitle);
		lblImportTitle.setFont(histogramFont);
		GridData importTitleGridData = new GridData(SWT.FILL, SWT.TOP, true, false);
		importTitleGridData.verticalIndent = 20;
		lblImportTitle.setLayoutData(importTitleGridData);

		Label separator2 = new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL);
		separator2.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		Composite importComposite = new Composite(getFieldEditorParent(), SWT.NONE);
		importComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		importComposite.setLayout(new GridLayout(3, false));

		Label lblImport = new Label(importComposite, SWT.NONE);
		lblImport.setText(Messages.LabSettings_importDirLabel);

		Text txtImportPath = new Text(importComposite, SWT.BORDER);
		txtImportPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		txtImportPath.setText(getPreferenceStore().getString("CFG_HL7_IMPORT_DIR")); //$NON-NLS-1$

		Button btnBrowse = new Button(importComposite, SWT.PUSH);
		btnBrowse.setText(Messages.LabSettings_browseButton);
		btnBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				org.eclipse.swt.widgets.DirectoryDialog dialog = new org.eclipse.swt.widgets.DirectoryDialog(
						getShell());
				String selectedDir = dialog.open();
				if (selectedDir != null) {
					txtImportPath.setText(selectedDir);
					getPreferenceStore().setValue("CFG_HL7_IMPORT_DIR", selectedDir); //$NON-NLS-1$
				}
			}
		});
		Composite profileComposite = new Composite(getFieldEditorParent(), SWT.NONE);
		profileComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		profileComposite.setLayout(new GridLayout(2, false));
		Label lblImportProfile = new Label(profileComposite, SWT.NONE);
		lblImportProfile.setText(Messages.LabSettings_importProfileLabel);
		Combo comboProfile = new Combo(profileComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		comboProfile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		String[] profiles = new String[] { "Analytica" }; // später z. B. "LaborXY", "Roche" etc. //$NON-NLS-1$
		comboProfile.setItems(profiles);
		String currentProfile = getPreferenceStore().getString("CFG_HL7_IMPORT_PROFILE"); //$NON-NLS-1$
		int index = Arrays.asList(profiles).indexOf(currentProfile);
		comboProfile.select(index >= 0 ? index : 0); // fallback auf erste Option
		comboProfile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String selectedProfile = comboProfile.getItem(comboProfile.getSelectionIndex());
				getPreferenceStore().setValue("CFG_HL7_IMPORT_PROFILE", selectedProfile); //$NON-NLS-1$
			}
		});

		Button btnAutoGroup = new Button(getFieldEditorParent(), SWT.PUSH);
		btnAutoGroup.setText(Messages.LabSettings_startAutoGroupButton);
		btnAutoGroup.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		btnAutoGroup.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String pfad = txtImportPath.getText();
				if (pfad == null || pfad.isEmpty()) {
					MessageDialog.openWarning(getShell(), Messages.LabSettings_pathMissingTitle,
							Messages.LabSettings_pathMissingMessage);
					return;
				}

				try {
					ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
					dialog.run(true, false, monitor -> {
						HL7AutoGroupImporter importer = new HL7AutoGroupImporter();
						importer.setProgressMonitor(monitor);

						java.util.concurrent.atomic.AtomicInteger anzahl = new java.util.concurrent.atomic.AtomicInteger();

						try {
							anzahl.set(importer.importDirectory(pfad));
						} catch (IOException ex) {
							ex.printStackTrace();
						}

						Display.getDefault().asyncExec(() -> {
							String msg = MessageFormat.format(Messages.LabSettings_importDoneMessage, anzahl.get());
							MessageDialog.openInformation(getShell(), Messages.LabSettings_importDoneTitle,
									msg);
						});
					});

				} catch (Exception ex) {
					MessageDialog.openError(getShell(), Messages.LabSettings_importErrorTitle,
							Messages.LabSettings_importErrorMessage + ex.getMessage());
					ex.printStackTrace();
				}
			}
		});

	}

	@Override
	public void dispose() {
		disposeResources();
		super.dispose();
	}

	private void disposeResources() {
		if (histogramFont != null && !histogramFont.isDisposed()) {
			histogramFont.dispose();
		}
	}

	public void init(final IWorkbench workbench) {
		daysKeepUnseen = ConfigServiceHolder.getGlobal(Preferences.LABSETTINGS_CFG_KEEP_UNSEEN_LAB_RESULTS, null);
		if (daysKeepUnseen == null || !isValidNumber(daysKeepUnseen)) {
			ConfigServiceHolder.get().set(Preferences.LABSETTINGS_CFG_KEEP_UNSEEN_LAB_RESULTS,
					Preferences.DAYS_TO_KEEP_UNSEEN_LAB_RESULTS);
			daysKeepUnseen = Preferences.DAYS_TO_KEEP_UNSEEN_LAB_RESULTS;
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if (event.getSource() instanceof FieldEditor) {
			FieldEditor fe = ((FieldEditor) event.getSource());
			if (Preferences.LABSETTINGS_CFG_LOCAL_REFVALUES.equals(fe.getPreferenceName())) {
				if (event.getNewValue().equals(Boolean.TRUE)) {
					MessageDialog.openInformation(UiDesk.getTopShell(),
							Messages.LabSettings_enableUseLocalLabRefValues_title,
							Messages.LabSettings_enableUseLocalLabRefValues_text);
				}
			}
		}
	}

	@Override
	public boolean performOk() {
		if (isValidNumber(txtKeepUnseen.getText())) {
			ConfigServiceHolder.get().set(Preferences.LABSETTINGS_CFG_KEEP_UNSEEN_LAB_RESULTS, txtKeepUnseen.getText());
		}
		disposeResources();
		ConfigServiceHolder.setUser(Preferences.LABSETTINGS_ANZAHL_MONATE, txtAnzahlMonate.getText());
		return super.performOk();
	}

	private boolean isValidNumber(String nrString) {
		try {
			Integer.parseInt(nrString);
			return true;
		} catch (NumberFormatException nfe) {
			log.warn(
					"Can't use [" + nrString + "] for KeepUnseen in lab settings as it can't be parsed to an integer."); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
	}
}
