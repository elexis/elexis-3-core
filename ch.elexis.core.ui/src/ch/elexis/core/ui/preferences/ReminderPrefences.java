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

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.preferences.inputs.DecoratedStringChooser;
import ch.elexis.core.ui.util.DecoratedString;
import ch.elexis.data.Reminder;
import ch.rgw.io.Settings;

public class ReminderPrefences extends PreferencePage implements IWorkbenchPreferencePage {
	Settings cfg;
	DecoratedString[] strings;
	private Button showRemindersOnPatientSelectionEventBtn;
	private ListViewer lViewerChoosen, lViewerAvailable;
	private String[] choosenFields;
	private String[] availableFields;
	private Label lblInfo;
	private String prefixPrevLabel = "Label Vorschau";
	
	public ReminderPrefences(){
		super(Messages.ReminderPrefences_Reminders);
		cfg = CoreHub.userCfg.getBranch(Preferences.USR_REMINDERCOLORS, true);
		strings = new DecoratedString[3];
		strings[0] = new DecoratedString(Reminder.STATE_PLANNED);
		strings[1] = new DecoratedString(Reminder.STATE_DUE);
		strings[2] = new DecoratedString(Reminder.STATE_OVERDUE);
		
		choosenFields =
			CoreHub.userCfg.get(Preferences.USR_REMINDER_PAT_LABEL_CHOOSEN,
				Reminder.LabelFields.LASTNAME.toString()).split(",");
		if (choosenFields.length == 3) {
			availableFields = new String[] {};
		} else {
			String defValue =
				Reminder.LabelFields.PAT_ID.toString() + ","
					+ Reminder.LabelFields.FIRSTNAME.toString();
			availableFields =
				CoreHub.userCfg.get(Preferences.USR_REMINDER_PAT_LABEL_AVAILABLE, defValue).split(
					",");
		}
		
	}
	
	@Override
	protected Control createContents(Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new GridLayout(3, true));
		new Label(ret, SWT.NONE).setText(Messages.ReminderPrefences_SetColors);
		DecoratedStringChooser chooser = new DecoratedStringChooser(ret, cfg, strings);
		chooser.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
		
		showRemindersOnPatientSelectionEventBtn = new Button(ret, SWT.CHECK);
		showRemindersOnPatientSelectionEventBtn
			.setText(Messages.ReminderPrefences_ShowPatientSelectionRedminders);
		showRemindersOnPatientSelectionEventBtn.setSelection(CoreHub.userCfg.get(
			Preferences.USR_SHOWPATCHGREMINDER, false));
		showRemindersOnPatientSelectionEventBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
			false, 3, 1));
		
		new Label(ret, SWT.NONE);
		new Label(ret, SWT.NONE);
		new Label(ret, SWT.NONE);
		
		Label lblSeparator = new Label(ret, SWT.HORIZONTAL | SWT.SEPARATOR);
		lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		Label lblLabelConfig = new Label(ret, SWT.NONE);
		lblLabelConfig.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		lblLabelConfig.setText("Pendenzen Label Optionen");
		
		Label lblChoosen = new Label(ret, SWT.NONE);
		lblChoosen.setText("Gewählt");
		new Label(ret, SWT.NONE);
		Label lblAvailable = new Label(ret, SWT.NONE);
		lblAvailable.setText("Noch verfügbar");
		
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
			public void widgetSelected(SelectionEvent e){
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
			public void widgetSelected(SelectionEvent e){
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
		return ret;
	}
	
	private String getPreviewLabel(){
		StringBuilder sb = new StringBuilder();
		for (String s : lViewerChoosen.getList().getItems()) {
			sb.append(s);
			sb.append(" ");
		}
		return prefixPrevLabel + ":\t" + sb.toString();
	}
	
	public void init(IWorkbench workbench){
		// TODO Auto-generated method stub
	}
	
	@Override
	public boolean performOk(){
		CoreHub.userCfg.set(Preferences.USR_SHOWPATCHGREMINDER,
			showRemindersOnPatientSelectionEventBtn.getSelection());
		
		CoreHub.userCfg.set(Preferences.USR_REMINDER_PAT_LABEL_CHOOSEN,
			getListAsString(lViewerChoosen.getList().getItems()));
		CoreHub.userCfg.set(Preferences.USR_REMINDER_PAT_LABEL_AVAILABLE,
			getListAsString(lViewerAvailable.getList().getItems()));
		
		CoreHub.userCfg.flush();
		return super.performOk();
	}
	
	private String getListAsString(String[] items){
		StringBuilder sb = new StringBuilder();
		for (String item : items) {
			sb.append(item);
			sb.append(",");
		}
		return sb.toString();
	}
	
}
