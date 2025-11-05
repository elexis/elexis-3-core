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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.preferences.inputs.DecoratedStringChooser;
import ch.elexis.core.ui.util.DecoratedString;
import ch.elexis.core.ui.views.reminder.viewers.ReminderColumnType;
import ch.elexis.data.Reminder;

public class ReminderPrefences extends PreferencePage implements IWorkbenchPreferencePage {
	DecoratedString[] strings;
	private Button showRemindersOnPatientSelectionEventBtn;
	private ListViewer lViewerChoosen, lViewerAvailable;
	private String[] choosenFields;
	private String[] availableFields;
	private Label lblInfo;
	private String prefixPrevLabel = Messages.ReminderPrefences_PrefixPrevLabel;
	private Button defaultPatientRelated;
	private Button defaultResponsibleSelf;
	private ListViewer lViewerVisible, lViewerHidden;

	public ReminderPrefences() {
		super(Messages.ReminderPrefences_Reminders);
		strings = new DecoratedString[5];
		strings[0] = new DecoratedString(ProcessStatus.OPEN.getLocaleText(), ProcessStatus.OPEN.name());
		strings[1] = new DecoratedString(ProcessStatus.IN_PROGRESS.getLocaleText(), ProcessStatus.IN_PROGRESS.name());
		strings[2] = new DecoratedString(ProcessStatus.DUE.getLocaleText(), ProcessStatus.DUE.name());
		strings[3] = new DecoratedString(ProcessStatus.OVERDUE.getLocaleText(), ProcessStatus.OVERDUE.name());
		strings[4] = new DecoratedString(ProcessStatus.CLOSED.getLocaleText(), ProcessStatus.CLOSED.name());
		choosenFields = ConfigServiceHolder
				.getUser(Preferences.USR_REMINDER_PAT_LABEL_CHOOSEN, Reminder.LabelFields.LASTNAME.toString())
				.split(","); //$NON-NLS-1$
		if (choosenFields.length == 3) {
			availableFields = new String[] {};
		} else {
			String defValue = Reminder.LabelFields.PAT_ID.toString() + "," + Reminder.LabelFields.FIRSTNAME.toString(); //$NON-NLS-1$
			availableFields = ConfigServiceHolder.getUser(Preferences.USR_REMINDER_PAT_LABEL_AVAILABLE, defValue)
					.split(","); //$NON-NLS-1$
		}

	}

	@Override
	protected Control createContents(Composite parent) {
		Composite ret = new Composite(parent, SWT.NONE);
		int nrElementsInTop = 3;
		ret.setLayout(new GridLayout(nrElementsInTop, true));
		new Label(ret, SWT.NONE).setText(Messages.ReminderPrefences_SetColors);
		DecoratedStringChooser chooser = new DecoratedStringChooser(ret, Preferences.USR_REMINDERCOLORS, strings);
		chooser.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, nrElementsInTop, 1));

		showRemindersOnPatientSelectionEventBtn = new Button(ret, SWT.CHECK);
		showRemindersOnPatientSelectionEventBtn.setText(Messages.ReminderPrefences_ShowPatientSelectionRedminders);
		showRemindersOnPatientSelectionEventBtn
				.setSelection(ConfigServiceHolder.getUser(Preferences.USR_SHOWPATCHGREMINDER, false));
		showRemindersOnPatientSelectionEventBtn
				.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, nrElementsInTop, 1));

		defaultPatientRelated = new Button(ret, SWT.CHECK);
		defaultPatientRelated.setText(ch.elexis.core.l10n.Messages.ReminderPref_defaultPatientRelated);
		defaultPatientRelated.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				ConfigServiceHolder.getUser(Preferences.USR_REMINDER_DEFAULT_PATIENT_RELATED,
						defaultPatientRelated.getSelection());
			}
		});
		defaultPatientRelated
				.setSelection(ConfigServiceHolder.getUser(Preferences.USR_REMINDER_DEFAULT_PATIENT_RELATED, true));
		defaultPatientRelated.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, nrElementsInTop, 1));

		defaultResponsibleSelf = new Button(ret, SWT.CHECK);
		defaultResponsibleSelf.setText(ch.elexis.core.l10n.Messages.ReminderPref_defaultReponsibleSelf);
		defaultResponsibleSelf.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				ConfigServiceHolder.getUser(Preferences.USR_REMINDER_DEFAULT_RESPONSIBLE_SELF,
						defaultResponsibleSelf.getSelection());
			}
		});
		defaultResponsibleSelf
				.setSelection(ConfigServiceHolder.getUser(Preferences.USR_REMINDER_DEFAULT_RESPONSIBLE_SELF, false));
		defaultResponsibleSelf.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, nrElementsInTop, 1));

		new Label(ret, SWT.NONE);
		new Label(ret, SWT.NONE);
		new Label(ret, SWT.NONE);

		Label lblSeparator = new Label(ret, SWT.HORIZONTAL | SWT.SEPARATOR);
		lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		Label lblLabelConfig = new Label(ret, SWT.NONE);
		lblLabelConfig.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		lblLabelConfig.setText(Messages.ReminderPrefences_LabelConfig);

		Label lblChoosen = new Label(ret, SWT.NONE);
		lblChoosen.setText(Messages.ReminderPrefences_Choosen);
		new Label(ret, SWT.NONE);
		Label lblAvailable = new Label(ret, SWT.NONE);
		lblAvailable.setText(Messages.ReminderPrefences_Available);

		GridData gdListViewer = new GridData();
		gdListViewer.horizontalAlignment = SWT.FILL;
		gdListViewer.verticalAlignment = SWT.CENTER;
		gdListViewer.minimumHeight = 100;
		gdListViewer.heightHint = 100;

		lViewerChoosen = new ListViewer(ret, SWT.BORDER | SWT.V_SCROLL);
		lViewerChoosen.getList().setLayoutData(gdListViewer);
		lViewerChoosen.setContentProvider(new ArrayContentProvider());
		lViewerChoosen.setInput(choosenFields);

		Composite btnComposite = new Composite(ret, SWT.NONE);
		btnComposite.setLayout(new GridLayout());
		btnComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));

		Button btnAdd = new Button(btnComposite, SWT.PUSH);
		btnAdd.setImage(Images.IMG_PREVIOUS.getImage());
		btnAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection sel = (IStructuredSelection) lViewerAvailable.getSelection();
				String item = (String) sel.getFirstElement();
				lViewerAvailable.remove(item);
				lViewerChoosen.add(item);
				lblInfo.setText(getPreviewLabel());
			}
		});

		Button btnRemove = new Button(btnComposite, SWT.PUSH);
		btnRemove.setImage(Images.IMG_NEXT.getImage());
		btnRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection sel = (IStructuredSelection) lViewerChoosen.getSelection();
				String item = (String) sel.getFirstElement();
				lViewerChoosen.remove(item);
				lViewerAvailable.add(item);
				lblInfo.setText(getPreviewLabel());
			}
		});

		lViewerAvailable = new ListViewer(ret, SWT.BORDER | SWT.V_SCROLL);
		lViewerAvailable.getList().setLayoutData(gdListViewer);
		lViewerAvailable.setContentProvider(new ArrayContentProvider());
		lViewerAvailable.setInput(availableFields);

		new Label(ret, SWT.NONE);
		new Label(ret, SWT.NONE);
		new Label(ret, SWT.NONE);

		lblInfo = new Label(ret, SWT.NONE);
		lblInfo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		lblInfo.setText(getPreviewLabel());

		Label lblSeparator2 = new Label(ret, SWT.HORIZONTAL | SWT.SEPARATOR);
		lblSeparator2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		Label lblColumnConfig = new Label(ret, SWT.NONE);
		lblColumnConfig.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		lblColumnConfig.setText(Messages.ReminderPrefences_ColumnConfig);

		Label lblVisible = new Label(ret, SWT.NONE);
		lblVisible.setText(Messages.ReminderPrefences_Visible);
		new Label(ret, SWT.NONE);
		Label lblHidden = new Label(ret, SWT.NONE);
		lblHidden.setText(Messages.ReminderPrefences_Hidden);

		GridData gdListViewerColumns = new GridData();
		gdListViewerColumns.horizontalAlignment = SWT.FILL;
		gdListViewerColumns.verticalAlignment = SWT.CENTER;
		gdListViewerColumns.minimumHeight = 100;
		gdListViewerColumns.heightHint = 100;

		lViewerVisible = new ListViewer(ret, SWT.BORDER | SWT.V_SCROLL);
		lViewerVisible.getList().setLayoutData(gdListViewerColumns);
		lViewerVisible.setContentProvider(ArrayContentProvider.getInstance());

		String defaultColumns = String.join(",", ReminderColumnType.getAllTitles()); //$NON-NLS-1$
		String[] visibleColumns = ConfigServiceHolder.getUser(Preferences.USR_REMINDER_COLUMNS_VISIBLE, defaultColumns)
				.split(","); //$NON-NLS-1$

		String hiddenValue = ConfigServiceHolder.getUser(Preferences.USR_REMINDER_COLUMNS_HIDDEN, StringUtils.EMPTY);
		String[] hiddenColumns = hiddenValue.split(","); //$NON-NLS-1$

		java.util.List<String> cleanedHidden = new java.util.ArrayList<>();
		for (String s : hiddenColumns) {
			if (s != null && !s.trim().isEmpty()) {
				cleanedHidden.add(s.trim());
			}
		}
		hiddenColumns = cleanedHidden.toArray(new String[0]);

		lViewerVisible.setInput(visibleColumns);

		Composite btnCompositeCols = new Composite(ret, SWT.NONE);
		btnCompositeCols.setLayout(new GridLayout());
		btnCompositeCols.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));

		Button btnAddCol = new Button(btnCompositeCols, SWT.PUSH);
		btnAddCol.setImage(Images.IMG_PREVIOUS.getImage());
		btnAddCol.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection sel = (IStructuredSelection) lViewerHidden.getSelection();
				String item = (String) sel.getFirstElement();
				if (item != null) {
					lViewerHidden.remove(item);
					lViewerVisible.add(item);
				}
			}
		});

		Button btnRemoveCol = new Button(btnCompositeCols, SWT.PUSH);
		btnRemoveCol.setImage(Images.IMG_NEXT.getImage());
		btnRemoveCol.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection sel = (IStructuredSelection) lViewerVisible.getSelection();
				String item = (String) sel.getFirstElement();
				if (item != null) {
					lViewerVisible.remove(item);
					lViewerHidden.add(item);
				}
			}
		});
		lViewerHidden = new ListViewer(ret, SWT.BORDER | SWT.V_SCROLL);
		lViewerHidden.getList().setLayoutData(gdListViewerColumns);
		lViewerHidden.setContentProvider(ArrayContentProvider.getInstance());
		lViewerHidden.setInput(hiddenColumns);

		new Label(ret, SWT.NONE);
		new Label(ret, SWT.NONE);
		new Label(ret, SWT.NONE);

		Label lblColInfo = new Label(ret, SWT.NONE);
		lblColInfo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		lblColInfo.setText(Messages.ReminderPrefences_ColInfo + String.join(", ", visibleColumns)); //$NON-NLS-1$

		return ret;
	}

	private String getPreviewLabel() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < lViewerChoosen.getList().getItems().length; i++) {
			sb.append(lViewerChoosen.getList().getItems()[i]);
			if (i < lViewerChoosen.getList().getItems().length - 1) {
				sb.append(", "); //$NON-NLS-1$
			}
		}
		return prefixPrevLabel + ":\t\t" + sb.toString(); //$NON-NLS-1$
	}

	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void performApply() {
		super.performApply();
	}

	@Override
	public boolean performOk() {
		ConfigServiceHolder.setUser(Preferences.USR_SHOWPATCHGREMINDER,
				showRemindersOnPatientSelectionEventBtn.getSelection());
		ConfigServiceHolder.setUser(Preferences.USR_REMINDER_DEFAULT_PATIENT_RELATED,
				defaultPatientRelated.getSelection());
		ConfigServiceHolder.setUser(Preferences.USR_REMINDER_DEFAULT_RESPONSIBLE_SELF,
				defaultResponsibleSelf.getSelection());
		ConfigServiceHolder.setUser(Preferences.USR_REMINDER_PAT_LABEL_CHOOSEN,
				getListAsString(lViewerChoosen.getList().getItems()));
		ConfigServiceHolder.setUser(Preferences.USR_REMINDER_PAT_LABEL_AVAILABLE,
				getListAsString(lViewerAvailable.getList().getItems()));
		ConfigServiceHolder.setUser(Preferences.USR_REMINDER_COLUMNS_VISIBLE,
				getListAsString(lViewerVisible.getList().getItems()));
		ConfigServiceHolder.setUser(Preferences.USR_REMINDER_COLUMNS_HIDDEN,
				getListAsString(lViewerHidden.getList().getItems()));
		ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_RELOAD, IReminder.class);
		return super.performOk();
	}

	private String getListAsString(String[] items) {
		StringBuilder sb = new StringBuilder();
		for (String item : items) {
			sb.append(item);
			sb.append(","); //$NON-NLS-1$
		}
		return sb.toString();
	}

}
