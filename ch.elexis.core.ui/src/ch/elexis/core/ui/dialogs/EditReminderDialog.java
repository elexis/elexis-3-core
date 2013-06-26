/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.dialogs;

import java.util.Date;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.data.Anwender;
import ch.elexis.core.data.Patient;
import ch.elexis.core.data.Reminder;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.icons.ImageSize;
import ch.elexis.core.icons.Images;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

import com.tiff.common.ui.datepicker.DatePickerCombo;

public class EditReminderDialog extends TitleAreaDialog {
	private static final String TX_ALL = Messages.getString("EditReminderDialog.all"); //$NON-NLS-1$
	Reminder mine;
	Text text;
	Label pat;
	DatePickerCombo dpDue;
	Button bDue, bDone, bRejected, bNoPatient;
	Combo cbType;
	List lUser;
	Patient actPatient;
	java.util.List<Anwender> users;
	
	public EditReminderDialog(final Shell parentShell, final Reminder rem){
		super(parentShell);
		mine = rem;
	}
	
	@Override
	protected Control createDialogArea(final Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout(2, false));
		new Label(ret, SWT.NONE).setText(Messages.getString("EditReminderDialog.assigTo")); //$NON-NLS-1$
		Composite cTopright = new Composite(ret, SWT.NONE);
		cTopright.setLayout(new RowLayout(SWT.HORIZONTAL));
		new Label(cTopright, SWT.NONE).setText(Messages.getString("EditReminderDialog.betrifft")); //$NON-NLS-1$
		pat = new Label(cTopright, SWT.NONE);
		pat.setText(Messages.getString("EditReminderDialog.noPatient")); //$NON-NLS-1$
		bNoPatient = new Button(cTopright, SWT.CHECK);
		bNoPatient.setText(Messages.getString("EditReminderDialog.noPatient")); //$NON-NLS-1$
		users = Hub.getUserList();
		lUser = new List(ret, SWT.MULTI | SWT.V_SCROLL);
		lUser.add(TX_ALL);
		for (Anwender a : users) {
			lUser.add(a.getLabel());
		}
		lUser.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		text = SWTHelper.createText(ret, 5, SWT.NONE);
		((GridData) lUser.getLayoutData()).heightHint = text.getLineHeight() * 5 + 10;
		
		new Label(ret, SWT.NONE).setText(Messages.getString("EditReminderDialog.actionwhenDue")); //$NON-NLS-1$
		
		cbType = new Combo(ret, SWT.SINGLE);
		for (String s : Reminder.TypText) {
			cbType.add(s);
		}
		cbType.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(final SelectionEvent e){
				// We check wether a letter reminder is selected and if so we
				// let the user choose the template
				if (cbType.getText().equals(Reminder.TypText[Reminder.Typ.brief.ordinal()])) {
					DocumentSelectDialog dsl =
						new DocumentSelectDialog(getShell(), CoreHub.actMandant,
							DocumentSelectDialog.TYPE_LOAD_TEMPLATE);
					if (dsl.open() == Dialog.OK) {
						mine.set("Params", dsl.getSelectedDocument().getId()); //$NON-NLS-1$
					}
				}
			}
		});
		Composite dates = new Composite(ret, SWT.NONE);
		dates.setLayout(new GridLayout(4, false));
		dates.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		
		new Label(dates, SWT.NONE).setText(Messages.getString("EditReminderDialog.dueOn")); //$NON-NLS-1$
		Label lbStatus = new Label(dates, SWT.NONE);
		lbStatus.setText(Messages.getString("EditReminderDialog.state")); //$NON-NLS-1$
		lbStatus.setLayoutData(SWTHelper.getFillGridData(3, true, 1, false));
		dpDue = new DatePickerCombo(dates, SWT.NONE);
		dpDue.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e){
				setLabels();
			}
		});
		bDue = new Button(dates, SWT.RADIO);
		bDue.setText(Messages.getString("EditReminderDialog.overDue")); //$NON-NLS-1$
		bDone = new Button(dates, SWT.RADIO);
		bDone.setText(Messages.getString("EditReminderDialog.fixed")); //$NON-NLS-1$
		bRejected = new Button(dates, SWT.RADIO);
		bRejected.setText(Messages.getString("EditReminderDialog.wontFix")); //$NON-NLS-1$
		
		initialize();
		setLabels();
		
		return ret;
	}
	
	/**
	 * Set initial values of controls
	 */
	private void initialize(){
		if (mine == null) {
			// new reminder
			
			actPatient = ElexisEventDispatcher.getSelectedPatient();
			text.setText(StringTool.leer);
			cbType.select(1);
			
			// select current user as responsible
			int index = 0;
			String usr = CoreHub.actUser.getLabel();
			for (int i = 0; i < lUser.getItemCount(); i++) {
				if (lUser.getItem(i).equals(usr)) {
					index = i;
					break;
				}
			}
			lUser.select(index);
			
			dpDue.setDate(new Date());
			bDue.setSelection(true);
		} else {
			// existing reminder
			
			actPatient = mine.getKontakt();
			text.setText(mine.get(Reminder.MESSAGE));
			cbType.select(mine.getTyp().ordinal());
			
			// select responsible
			int index = 0;
			String uid = mine.get(Reminder.RESPONSIBLE);
			java.util.List<Anwender> responsibles = mine.getResponsibles();
			if (responsibles.size() > 0) { // new method
				for (Anwender a : responsibles) {
					int idx = StringTool.getIndex(lUser.getItems(), a.getLabel());
					if (idx != -1) {
						lUser.select(idx);
					}
				}
			} else if (!StringTool.isNothing(uid)) { // to be removed later
				String usr = Anwender.load(uid).getLabel();
				for (int i = 0; i < lUser.getItemCount(); i++) {
					if (lUser.getItem(i).equals(usr)) {
						index = i;
						break;
					}
				}
				lUser.select(index);
			}
			
			dpDue.setDate(mine.getDateDue().getTime());
			
			// update current selection depending on the status
			Reminder.Status s = mine.getStatus();
			if (s.equals(Reminder.Status.STATE_DONE)) {
				bDone.setSelection(true);
			} else if (s.equals(Reminder.Status.STATE_UNDONE)) {
				bRejected.setSelection(true);
			} else {
				bDue.setSelection(true);
			}
		}
		
		if (actPatient == null) {
			pat.setText(Messages.getString("EditReminderDialog.noPatientSelected")); //$NON-NLS-1$
		} else {
			pat.setText("  " + actPatient.getLabel() + "  "); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	/**
	 * Update label of bDue depending on the selected time
	 */
	private void setLabels(){
		if (mine == null) {
			// new reminder
			bDue.setText(Messages.getString("EditReminderDialog.scheduled")); //$NON-NLS-1$
		} else {
			TimeTool today = new TimeTool();
			TimeTool tSelected = new TimeTool(dpDue.getDate().getTime());
			
			if (today.isSameDay(tSelected)) {
				bDue.setText(Messages.getString("EditReminderDialog.due")); //$NON-NLS-1$
			} else if (today.isBefore(tSelected)) {
				bDue.setText(Messages.getString("EditReminderDialog.open")); //$NON-NLS-1$
			} else {
				bDue.setText(Messages.getString("EditReminderDialog.overdue")); //$NON-NLS-1$
			}
		}
	}
	
	@Override
	public void create(){
		super.create();
		String shelltitle = Messages.getString("EditReminderDialog.reminderShellTitle"); //$NON-NLS-1$
		if (mine == null) {
			setTitle(Messages.getString("EditReminderDialog.createReminder")); //$NON-NLS-1$
		} else {
			setTitle(Messages.getString("EditReminderDialog.editReminder")); //$NON-NLS-1$
			Anwender o = mine.getCreator();
			if (o == null) {
				shelltitle += Messages.getString("EditReminderDialog.unknown"); //$NON-NLS-1$
			} else {
				shelltitle += " (" + o.getLabel() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		setMessage(Messages.getString("EditReminderDialog.enterDataForReminder")); //$NON-NLS-1$
		getShell().setText(shelltitle);
		setTitleImage(Images.IMG_LOGO.getImage(ImageSize._75x66_TitleDialogIconSize));
	}
	
	@Override
	protected void okPressed(){
		if (bNoPatient.getSelection()) {
			actPatient = null;
		}
		String due = new TimeTool(dpDue.getDate().getTime()).toString(TimeTool.DATE_GER);
		int typidx = cbType.getSelectionIndex();
		if (typidx == -1) {
			typidx = 0;
		}
		Reminder.Typ typ = Reminder.Typ.values()[typidx];
		if (mine == null) {
			mine = new Reminder(actPatient, due, typ, "", text.getText()); //$NON-NLS-1$
		} else {
			mine.set(new String[] {
				Reminder.KONTAKT_ID, Reminder.DUE, Reminder.TYPE, Reminder.MESSAGE
			}, new String[] {
				actPatient.getId(), due, Byte.toString((byte) typ.ordinal()), text.getText()
			});
		}
		if (bDone.getSelection()) {
			mine.setStatus(Reminder.Status.STATE_DONE);
		} else if (bRejected.getSelection()) {
			mine.setStatus(Reminder.Status.STATE_UNDONE);
		} else {
			mine.setStatus(Reminder.Status.STATE_PLANNED);
		}
		int[] resps = lUser.getSelectionIndices();
		
		// Delete all responsible persons before setting the new
		for (Anwender anwender : mine.getResponsibles()) {
			mine.removeResponsible(anwender);
		}
		// Set the new List of responsible persons
		if (resps.length > 0) {
			if (resps[0] == 0) { // If none select set all users ("Alle")
				for (Anwender a : users) {
					mine.addResponsible(a);
				}
			} else {
				for (int i = 0; i < resps.length; i++) {
					int idx = resps[i];
					Anwender a = users.get(idx - 1);
					mine.addResponsible(a);
				}
			}
		}
		super.okPressed();
	}
	
}
