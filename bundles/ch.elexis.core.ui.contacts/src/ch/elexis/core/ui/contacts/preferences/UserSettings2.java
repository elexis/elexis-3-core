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

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.model.IUserConfig;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQueryCursor;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.StickerServiceHolder;
import ch.elexis.core.ui.constants.UiResourceConstants;
import ch.elexis.core.ui.contacts.views.Patientenblatt2;
import ch.elexis.core.ui.preferences.ConfigServicePreferenceStore;
import ch.elexis.core.ui.preferences.ConfigServicePreferenceStore.Scope;
import ch.elexis.core.ui.preferences.inputs.ComboFieldEditor;
import ch.elexis.core.ui.preferences.inputs.MultilineFieldEditor;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Patient;
import ch.elexis.data.Sticker;
import ch.rgw.tools.StringTool;

public class UserSettings2 extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public static final String REMEMBER_LAST_STATE = Messages.UserSettings2_RememberLastState;
	public static final String ALWAYS_CLOSED = Messages.UserSettings2_AlwaysClosed;
	public static final String ALWAYS_OPEN = Messages.UserSettings2_AlwaysOpen;

	private ConfigServicePreferenceStore prefs = new ConfigServicePreferenceStore(Scope.USER);
	private ConfigServicePreferenceStore globalPrefs = new ConfigServicePreferenceStore(Scope.GLOBAL);

	private boolean extraBool;

	private static final String[] patlistFocusFields = { Patient.FLD_PATID, Patient.FLD_NAME, Patient.FLD_FIRSTNAME,
			Patient.BIRTHDATE, };

	public UserSettings2() {
		super(GRID);
		setPreferenceStore(prefs);
		prefs.setDefault(USERSETTINGS2_EXPANDABLE_COMPOSITES, USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_REMEMBER_STATE);
		prefs.setDefault(Preferences.USR_PATLIST_SHOWPATNR, false);
		prefs.setDefault(Preferences.USR_PATLIST_SHOWNAME, true);
		prefs.setDefault(Preferences.USR_PATLIST_SHOWFIRSTNAME, true);
		prefs.setDefault(Preferences.USR_PATLIST_SHOWDOB, true);
		prefs.setDefault(Preferences.USR_SUPPRESS_INTERACTION_CHECK, true);
		prefs.setDefault(Patientenblatt2.CFG_GLOBALFIELDS, false);
	}

	@Override
	protected void createFieldEditors() {
		addField(new RadioGroupFieldEditor(USERSETTINGS2_EXPANDABLE_COMPOSITES, Messages.UserSettings2_ExtendableFields,
				1,
				new String[][] { { ALWAYS_OPEN, USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_OPEN },
						{ ALWAYS_CLOSED, USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_CLOSED },
						{ REMEMBER_LAST_STATE, USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_REMEMBER_STATE }

				}, getFieldEditorParent()));
		new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL)
				.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		new Label(getFieldEditorParent(), SWT.NONE).setText(Messages.UserSettings2_FieldsInList);
		addField(new BooleanFieldEditor(Preferences.USR_PATLIST_SHOWPATNR, Messages.Core_Patient_Number,
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(Preferences.USR_PATLIST_SHOWNAME, Messages.Core_Name,
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(Preferences.USR_PATLIST_SHOWFIRSTNAME, Messages.Core_Firstname,
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(Preferences.USR_PATLIST_SHOWDOB, Messages.Core_Enter_Birthdate,
				getFieldEditorParent()));
		addField(new ComboFieldEditor(Preferences.USR_PATLIST_FOCUSFIELD, "Fokusfeld", patlistFocusFields,
				getFieldEditorParent()));
		new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL)
				.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		Composite container = new Composite(getFieldEditorParent(), SWT.NONE);
		container.setLayout(new GridLayout(2, false));
		new Label(container, SWT.NONE).setText(Messages.UserSettings2_AddidtionalFields);
		extraBool = prefs.getBoolean(Patientenblatt2.CFG_GLOBALFIELDS);
		Button checkBox = new Button(container, SWT.CHECK);
		checkBox.setText("globale Felder"); //$NON-NLS-1$
		checkBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		checkBox.setSelection(extraBool);
		MultilineFieldEditor zusatzFeldEditor = new MultilineFieldEditor(Patientenblatt2.CFG_EXTRAFIELDS,
				StringTool.leer, 5, SWT.NONE, true, getFieldEditorParent());
		setZusatzFeldStore(zusatzFeldEditor);
		addField(zusatzFeldEditor);
		checkBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				extraBool = checkBox.getSelection();
				prefs.setValue(Patientenblatt2.CFG_GLOBALFIELDS, extraBool);
				setZusatzFeldStore(zusatzFeldEditor);
				if (zusatzFeldEditor.getPreferenceStore().equals(globalPrefs)
						&& globalPrefs.getString(Patientenblatt2.CFG_EXTRAFIELDS).isEmpty()) {
					String globalFields = getAllExtraFields();
					zusatzFeldEditor.getTextControl(getFieldEditorParent()).setText(globalFields.replace(",", "\r\n")); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					zusatzFeldEditor.getTextControl(getFieldEditorParent()).setText(zusatzFeldEditor
							.getPreferenceStore().getString(Patientenblatt2.CFG_EXTRAFIELDS).replace(",", "\r\n")); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		});
		new Label(getFieldEditorParent(), SWT.NONE).setText(StringUtils.EMPTY);
		ComboFieldEditor editor = new ComboFieldEditor(Preferences.CFG_DECEASED_STICKER, "Sticker für verstorbene",
				getStickerComboItems(), getFieldEditorParent());
		editor.setPreferenceStore(new ConfigServicePreferenceStore(Scope.GLOBAL));
		addField(editor);
		Button migrateButton = new Button(getFieldEditorParent(), SWT.PUSH);
		migrateButton.setText("Verstorben Info aus Sticker übernehmen");
		migrateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String cfgSticker = editor.getCombo().getItem(editor.getCombo().getSelectionIndex());
				ISticker sticker = getSticker(cfgSticker);
				if (sticker != null) {
					if (MessageDialog.openQuestion(getShell(), "Verstorben Info", "Soll bei Patienten mit dem Sticker "
							+ cfgSticker
							+ " die verstorben Information gesetzt werden?\nAchtung, der Vorgang kann nicht rückgängig gemacht werden.")) {
						ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(getShell());
						try {
							progressDialog.run(true, true, new IRunnableWithProgress() {
								@Override
								public void run(IProgressMonitor monitor)
										throws InvocationTargetException, InterruptedException {
									IQuery<IPatient> query = CoreModelServiceHolder.get().getQuery(IPatient.class);
									try (IQueryCursor<IPatient> cursor = query.executeAsCursor()) {
										monitor.beginTask("Verstorben Migration", cursor.size());
										while (cursor.hasNext()) {
											IPatient patient = cursor.next();
											ISticker existing = StickerServiceHolder.get().getSticker(patient, sticker);
											if (existing != null && !patient.isDeceased()) {
												patient.setDeceased(true);
												CoreModelServiceHolder.get().save(patient);
											}
											monitor.worked(1);
										}
									}
								}
							});
						} catch (InvocationTargetException | InterruptedException ex) {
							MessageDialog.openError(getShell(), "Verstorben Sticker",
									"Fehler beim verstorben Information setzen.");
							LoggerFactory.getLogger(getClass()).error("Error migration of deceased sticker", ex); //$NON-NLS-1$
						}
					}
				} else {
					MessageDialog.openError(getShell(), "Verstorben Sticker",
							"Es konnte kein Sticker mit dem Namen " + cfgSticker + " für Patient gefunden werden.");
				}
			}
		});
		editor.getCombo().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				migrateButton.setEnabled(editor.getCombo().getSelectionIndex() > 0);
			}
		});
		migrateButton.setEnabled(StringUtils
				.isNotBlank(ConfigServiceHolder.getGlobal(Preferences.CFG_DECEASED_STICKER, StringUtils.EMPTY)));
	}

	private String getAllExtraFields() {
		IQuery<IUserConfig> ret = CoreModelServiceHolder.get().getQuery(IUserConfig.class);
		ret.and("Param", COMPARATOR.EQUALS, Patientenblatt2.CFG_EXTRAFIELDS); //$NON-NLS-1$
		List<IUserConfig> list = ret.execute();
		return list.stream().map(uc -> uc.getValue()).collect(Collectors.joining(",")); //$NON-NLS-1$
	}

	private void setZusatzFeldStore(MultilineFieldEditor zusatzFeldEditor) {
		if (prefs.getBoolean(Patientenblatt2.CFG_GLOBALFIELDS)) {
			zusatzFeldEditor.setPreferenceStore(globalPrefs);
		} else {
			zusatzFeldEditor.setPreferenceStore(prefs);
		}
	}

	private String[] getStickerComboItems() {
		List<Sticker> stickers = Sticker.getStickersForClass(Patient.class);
		List<String> stickerNames = stickers.stream().map(s -> s.getLabel()).collect(Collectors.toList());
		stickerNames.add(0, StringUtils.EMPTY);
		return stickerNames.toArray(new String[stickerNames.size()]);
	}

	private ISticker getSticker(String stickername) {
		for (ISticker iSticker : StickerServiceHolder.get().getStickersForClass(IPatient.class)) {
			if (iSticker.getName().equalsIgnoreCase(stickername)) {
				return iSticker;
			}
		}
		return null;
	}

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean performOk() {
		SWTHelper.reloadViewPart(UiResourceConstants.PatientenListeView_ID);
		SWTHelper.reloadViewPart(UiResourceConstants.PatientDetailView2_ID);
		return super.performOk();
	}
}
