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
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.Reminder;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.preferences.inputs.DecoratedStringChooser;
import ch.elexis.core.ui.util.DecoratedString;
import ch.rgw.io.Settings;

public class ReminderPrefences extends PreferencePage implements IWorkbenchPreferencePage {
	Settings cfg;
	DecoratedString[] strings;
	private Button showRemindersOnPatientSelectionEventBtn;
	
	public ReminderPrefences(){
		super(Messages.ReminderPrefences_Reminders);
		cfg = CoreHub.userCfg.getBranch(Preferences.USR_REMINDERCOLORS, true);
		strings = new DecoratedString[3];
		strings[0] = new DecoratedString(Reminder.STATE_PLANNED);
		strings[1] = new DecoratedString(Reminder.STATE_DUE);
		strings[2] = new DecoratedString(Reminder.STATE_OVERDUE);
	}
	
	@Override
	protected Control createContents(Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new GridLayout());
		new Label(ret, SWT.NONE).setText(Messages.ReminderPrefences_SetColors);
		DecoratedStringChooser chooser = new DecoratedStringChooser(ret, cfg, strings);
		chooser.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		showRemindersOnPatientSelectionEventBtn = new Button(ret, SWT.CHECK);
		showRemindersOnPatientSelectionEventBtn
			.setText(Messages.ReminderPrefences_ShowPatientSelectionRedminders);
		showRemindersOnPatientSelectionEventBtn.setSelection(CoreHub.userCfg.get(
			Preferences.USR_SHOWPATCHGREMINDER, false));
		showRemindersOnPatientSelectionEventBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
			false));
		
		return ret;
	}
	
	public void init(IWorkbench workbench){
		// TODO Auto-generated method stub
	}
	
	@Override
	public boolean performOk(){
		CoreHub.userCfg.set(Preferences.USR_SHOWPATCHGREMINDER,
			showRemindersOnPatientSelectionEventBtn.getSelection());
		CoreHub.userCfg.flush();
		return super.performOk();
	}
	
}
