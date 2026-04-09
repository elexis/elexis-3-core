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
import ch.elexis.core.ui.views.reminder.viewers.ReminderColumnType.ReminderColorType;
import ch.elexis.data.Reminder;

public class ReminderPrefences extends PreferencePage implements IWorkbenchPreferencePage {
	private DecoratedString[] strings;
	private Button showRemindersOnPatientSelectionEventBtn;
	private ListViewer lViewerChoosen, lViewerAvailable;
	private String[] choosenFields;
	private String[] availableFields;
	private Label lblInfo;
	private String prefixPrevLabel = Messages.ReminderPrefences_PrefixPrevLabel;
	private Button defaultPatientRelated;
	private Button defaultResponsibleSelf;
	private ListViewer lViewerVisible, lViewerHidden, lViewerCustomStatuses;

	private Composite chooserParent;
	private DecoratedStringChooser chooser;

	public ReminderPrefences() {
		super(Messages.ReminderPrefences_Reminders);

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

		chooserParent = new Composite(ret, SWT.NONE);
		GridLayout cpLayout = new GridLayout(1, false);
		cpLayout.marginWidth = 0;
		cpLayout.marginHeight = 0;
		chooserParent.setLayout(cpLayout);
		chooserParent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, nrElementsInTop, 1));

		refreshColorChooser(false);

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
				ConfigServiceHolder.setUser(Preferences.USR_REMINDER_DEFAULT_PATIENT_RELATED,
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
				ConfigServiceHolder.setUser(Preferences.USR_REMINDER_DEFAULT_RESPONSIBLE_SELF,
						defaultResponsibleSelf.getSelection());
			}
		});
		defaultResponsibleSelf
				.setSelection(ConfigServiceHolder.getUser(Preferences.USR_REMINDER_DEFAULT_RESPONSIBLE_SELF, false));
		defaultResponsibleSelf.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, nrElementsInTop, 1));

		Label lblSeparatorCustom = new Label(ret, SWT.HORIZONTAL | SWT.SEPARATOR);
		lblSeparatorCustom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		Label lblCustomTitle = new Label(ret, SWT.NONE);
		lblCustomTitle.setText(Messages.ReminderPrefences_CustomStatusesTitle);
		lblCustomTitle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		Composite customStatusInputComp = new Composite(ret, SWT.NONE);
		customStatusInputComp.setLayout(new GridLayout(3, false));
		customStatusInputComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		org.eclipse.swt.widgets.Text txtNewStatus = new org.eclipse.swt.widgets.Text(customStatusInputComp, SWT.BORDER);
		txtNewStatus.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		txtNewStatus.setMessage(Messages.ReminderPrefences_NewStatusMessage);

		Button btnAddStatus = new Button(customStatusInputComp, SWT.PUSH);
		btnAddStatus.setImage(Images.IMG_NEW.getImage());
		btnAddStatus.setToolTipText(Messages.ReminderPrefences_AddStatusTooltip);

		Button btnRemoveStatus = new Button(customStatusInputComp, SWT.PUSH);
		btnRemoveStatus.setImage(Images.IMG_DELETE.getImage());
		btnRemoveStatus.setToolTipText(Messages.ReminderPrefences_RemoveStatusTooltip);

		lViewerCustomStatuses = new ListViewer(ret, SWT.BORDER | SWT.V_SCROLL);
		GridData gdCustomList = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
		gdCustomList.heightHint = 80;
		lViewerCustomStatuses.getList().setLayoutData(gdCustomList);
		lViewerCustomStatuses.setContentProvider(new ArrayContentProvider());

		String loadedCustoms = ConfigServiceHolder.getGlobal(Preferences.USR_REMINDER_CUSTOM_STATUSES_GLOBAL,
				StringUtils.EMPTY);
		String[] customArray = loadedCustoms.isEmpty() ? new String[0] : loadedCustoms.split(",");
		lViewerCustomStatuses.setInput(new java.util.ArrayList<>(java.util.Arrays.asList(customArray)));

		txtNewStatus.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_RETURN) {
				e.doit = false;
				
				String newStat = txtNewStatus.getText().trim();
				newStat = newStat.replace(",", StringUtils.EMPTY);

				if (!newStat.isEmpty()) {
					lViewerCustomStatuses.add(newStat);
					txtNewStatus.setText(StringUtils.EMPTY);
					refreshColorChooser(true);
				}
			}
		});

		btnAddStatus.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String newStat = txtNewStatus.getText().trim();
				newStat = newStat.replace(",", StringUtils.EMPTY);

				if (!newStat.isEmpty()) {
					lViewerCustomStatuses.add(newStat);
					txtNewStatus.setText(StringUtils.EMPTY);
					refreshColorChooser(true);
				}
			}
		});

		btnRemoveStatus.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection sel = (IStructuredSelection) lViewerCustomStatuses.getSelection();
				if (!sel.isEmpty()) {
					lViewerCustomStatuses.remove(sel.getFirstElement());
					refreshColorChooser(true);
				}
			}
		});


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

	private void refreshColorChooser(boolean forceLayout) {
		if (chooser != null && !chooser.isDisposed()) {
			chooser.dispose();
		}

		java.util.List<DecoratedString> dynStrings = new java.util.ArrayList<>();
		dynStrings.add(new DecoratedString(ProcessStatus.OPEN.getLocaleText(), ProcessStatus.OPEN.name()));
		dynStrings
				.add(new DecoratedString(ProcessStatus.IN_PROGRESS.getLocaleText(), ProcessStatus.IN_PROGRESS.name()));
		dynStrings.add(new DecoratedString(ProcessStatus.DUE.getLocaleText(), ProcessStatus.DUE.name()));
		dynStrings.add(new DecoratedString(ProcessStatus.OVERDUE.getLocaleText(), ProcessStatus.OVERDUE.name()));
		dynStrings.add(new DecoratedString(ProcessStatus.CLOSED.getLocaleText(), ProcessStatus.CLOSED.name()));
		dynStrings.add(new DecoratedString(Messages.ReminderPrefences_DateFuture, ReminderColorType.FUTURE.name()));
		dynStrings.add(new DecoratedString(Messages.ReminderPrefences_DateNoDate, ReminderColorType.NO_DATE.name()));

		if (lViewerCustomStatuses != null && !lViewerCustomStatuses.getControl().isDisposed()) {
			String[] customArray = lViewerCustomStatuses.getList().getItems();
			for (String custom : customArray) {
				dynStrings.add(new DecoratedString(custom, Preferences.USR_REMINDER_CUSTOM_COLOR_PREFIX + custom));
			}
		} else {
			String globalCustoms = ConfigServiceHolder.getGlobal(Preferences.USR_REMINDER_CUSTOM_STATUSES_GLOBAL,
					StringUtils.EMPTY);
			String[] customArray = globalCustoms.isEmpty() ? new String[0] : globalCustoms.split(",");
			for (String custom : customArray) {
				dynStrings.add(new DecoratedString(custom, Preferences.USR_REMINDER_CUSTOM_COLOR_PREFIX + custom));
			}
		}

		strings = dynStrings.toArray(new DecoratedString[0]);

		chooser = new DecoratedStringChooser(chooserParent, Preferences.USR_REMINDERCOLORS, strings);
		chooser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		if (forceLayout) {
			Composite current = chooserParent;
			while (current != null) {
				current.layout(true, true);
				if (current instanceof org.eclipse.swt.custom.ScrolledComposite) {
					org.eclipse.swt.custom.ScrolledComposite sc = (org.eclipse.swt.custom.ScrolledComposite) current;
					sc.setMinSize(sc.getContent().computeSize(SWT.DEFAULT, SWT.DEFAULT));
					break;
				}
				current = current.getParent();
			}
		}
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
		saveData();
		super.performApply();
	}

	@Override
	public boolean performOk() {
		saveData();
		ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_RELOAD, IReminder.class);
		return super.performOk();
	}

	private void saveData() {
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

		String[] currentCustomStatuses = lViewerCustomStatuses.getList().getItems();
		String joinedCustoms = String.join(",", currentCustomStatuses);
		ConfigServiceHolder.get().set(Preferences.USR_REMINDER_CUSTOM_STATUSES_GLOBAL, joinedCustoms);
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