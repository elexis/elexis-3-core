package ch.elexis.core.ui.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.tiff.common.ui.datepicker.DatePickerCombo;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.model.issue.Type;
import ch.elexis.core.model.issue.Priority;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.model.issue.Visibility;
import ch.elexis.core.ui.icons.ImageSize;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.data.Anwender;
import ch.elexis.data.Patient;
import ch.elexis.data.Reminder;
import ch.rgw.tools.TimeTool;

public class ReminderDetailDialog extends TitleAreaDialog {
	
	private static final String TX_ALL = Messages.EditReminderDialog_all; //$NON-NLS-1$
	
	private Reminder reminder;
	private Patient patient;
	private Priority priority;
	private ProcessStatus processStatus;
	
	private Text txtSubject;
	private Text txtDescription;
	private DatePickerCombo dateDuePicker;
	private Label lblRelatedPatient;
	private Button[] btnPriorities = new Button[3];
	private Button[] btnProcessStatus = new Button[6];
	private ComboViewer cvActionType;
	private ComboViewer cvVisibility;
	private Button btnNotPatientRelated;
	private ListViewer lvResponsible;
	
	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 * @wbp.parser.constructor
	 */
	public ReminderDetailDialog(Shell parentShell){
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.MIN | SWT.RESIZE);
	}
	
	public ReminderDetailDialog(Shell parentShell, Reminder reminder){
		this(parentShell);
		this.reminder = reminder;
	}
	
	@Override
	public void create(){
		super.create();
		String shelltitle = Messages.EditReminderDialog_reminderShellTitle; //$NON-NLS-1$
		if (reminder == null) {
			setTitle(Messages.EditReminderDialog_createReminder); //$NON-NLS-1$
		} else {
			setTitle(Messages.EditReminderDialog_editReminder); //$NON-NLS-1$
			Anwender o = reminder.getCreator();
			if (o == null) {
				shelltitle += Messages.EditReminderDialog_unknown; //$NON-NLS-1$
			} else {
				shelltitle += " (" + o.getLabel() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		getShell().setText(shelltitle);
		
		setMessage(Messages.EditReminderDialog_enterDataForReminder);
		setTitleImage(Images.lookupImage("tick_banner.png", ImageSize._75x66_TitleDialogIconSize));
	}
	
	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent){
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite compositeResponsible = new Composite(container, SWT.NONE);
		compositeResponsible.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1));
		compositeResponsible.setBounds(0, 0, 64, 64);
		GridLayout gl_compositeResponsible = new GridLayout(1, false);
		gl_compositeResponsible.horizontalSpacing = 0;
		gl_compositeResponsible.marginTop = 2;
		gl_compositeResponsible.marginWidth = 0;
		gl_compositeResponsible.marginHeight = 0;
		compositeResponsible.setLayout(gl_compositeResponsible);
		
		Label lblResponsible = new Label(compositeResponsible, SWT.NONE);
		lblResponsible.setText(Messages.EditReminderDialog_assigTo);
		
		lvResponsible = new ListViewer(compositeResponsible, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		GridData gd_listResponsible = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_listResponsible.heightHint = 150;
		lvResponsible.getList().setLayoutData(gd_listResponsible);
		lvResponsible.setContentProvider(ArrayContentProvider.getInstance());
		lvResponsible.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof Anwender) {
					Anwender anw = (Anwender) element;
					return anw.getLabel();
				}
				return element.toString();
			}
		});
		List<Object> inputList = new ArrayList<Object>();
		inputList.add(TX_ALL);
		inputList.addAll(CoreHub.getUserList());
		lvResponsible.setInput(inputList);
		
		Composite compositeMessage = new Composite(container, SWT.NONE);
		GridLayout gl_compositeMessage = new GridLayout(1, false);
		gl_compositeMessage.marginWidth = 0;
		gl_compositeMessage.marginHeight = 0;
		compositeMessage.setLayout(gl_compositeMessage);
		compositeMessage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		compositeMessage.setBounds(0, 0, 64, 64);
		
		Composite compositeMessageHead = new Composite(compositeMessage, SWT.NONE);
		compositeMessageHead.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		compositeMessageHead.setBounds(0, 0, 64, 64);
		GridLayout gl_compositeMessageHead = new GridLayout(2, false);
		gl_compositeMessageHead.marginWidth = 0;
		gl_compositeMessageHead.marginHeight = 0;
		compositeMessageHead.setLayout(gl_compositeMessageHead);
		
		lblRelatedPatient = new Label(compositeMessageHead, SWT.NONE);
		lblRelatedPatient.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		btnNotPatientRelated = new Button(compositeMessageHead, SWT.CHECK);
		btnNotPatientRelated.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnNotPatientRelated.setText(Messages.EditReminderDialog_noPatient);
		
		txtSubject = new Text(compositeMessage, SWT.BORDER);
		txtSubject.setMessage(Messages.ReminderDetailDialog_txtSubject_message);
		txtSubject.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtSubject.setTextLimit(160);
		
		txtDescription = new Text(compositeMessage, SWT.BORDER | SWT.WRAP);
		txtDescription.setMessage("description");
		txtDescription.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		txtDescription.setBounds(0, 0, 64, 19);
		
		Composite composite = new Composite(compositeMessage, SWT.NONE);
		GridLayout gl_composite = new GridLayout(2, false);
		gl_composite.verticalSpacing = 0;
		gl_composite.marginWidth = 0;
		gl_composite.marginHeight = 0;
		composite.setLayout(gl_composite);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		Label visibleImage = new Label(composite, SWT.None);
		visibleImage.setImage(Images.IMG_EYE_WO_SHADOW.getImage());
		
		cvVisibility = new ComboViewer(composite, SWT.NONE);
		Combo combo = cvVisibility.getCombo();
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cvVisibility.setContentProvider(ArrayContentProvider.getInstance());
		cvVisibility.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				Visibility vis = (Visibility) element;
				return vis.getLocaleText();
			}
		});
		
		Composite compositeSettings = new Composite(container, SWT.BORDER);
		compositeSettings.setLayout(new GridLayout(2, false));
		compositeSettings.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		compositeSettings.setBounds(0, 0, 64, 64);
		
		Group grpState = new Group(compositeSettings, SWT.NONE);
		grpState.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		grpState.setText(Messages.EditReminderDialog_state);
		grpState.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		SelectionListener processStatusListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				ProcessStatus ps = (ProcessStatus) ((Button) e.widget).getData();
				setReminderStatus(ps);
			}
		};
		
		Button btnOpen = new Button(grpState, SWT.RADIO);
		btnOpen.setText(ProcessStatus.OPEN.getLocaleText());
		btnOpen.setData(ProcessStatus.OPEN);
		btnOpen.addSelectionListener(processStatusListener);
		btnProcessStatus[0] = btnOpen;
		
		Button btnInProgress = new Button(grpState, SWT.RADIO);
		btnInProgress.setText(ProcessStatus.IN_PROGRESS.getLocaleText());
		btnInProgress.setData(ProcessStatus.IN_PROGRESS);
		btnInProgress.addSelectionListener(processStatusListener);
		btnProcessStatus[1] = btnInProgress;
		
		Button btnClosed = new Button(grpState, SWT.RADIO);
		btnClosed.setText(ProcessStatus.CLOSED.getLocaleText());
		btnClosed.setData(ProcessStatus.CLOSED);
		btnClosed.addSelectionListener(processStatusListener);
		btnProcessStatus[2] = btnClosed;
		
		Button btnOnHold = new Button(grpState, SWT.RADIO);
		btnOnHold.setText(ProcessStatus.ON_HOLD.getLocaleText());
		btnOnHold.setData(ProcessStatus.ON_HOLD);
		btnOnHold.addSelectionListener(processStatusListener);
		btnProcessStatus[3] = btnOnHold;
		
		Button btnDue = new Button(grpState, SWT.RADIO);
		btnDue.setForeground(SWTResourceManager.getColor(255, 69, 0));
		btnDue.setText(ProcessStatus.DUE.getLocaleText());
		btnDue.setEnabled(false);
		btnDue.setData(ProcessStatus.DUE);
		btnProcessStatus[4] = btnDue;
		
		Button btnOverdue = new Button(grpState, SWT.RADIO);
		btnOverdue.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		btnOverdue.setText(ProcessStatus.OVERDUE.getLocaleText());
		btnOverdue.setEnabled(false);
		btnOverdue.setData(ProcessStatus.OVERDUE);
		btnProcessStatus[5] = btnOverdue;
		
		Group grpPriority = new Group(compositeSettings, SWT.NONE);
		grpPriority.setText(Messages.ReminderDetailDialog_grpPriority_text);
		grpPriority.setLayout(new RowLayout(SWT.HORIZONTAL));
		grpPriority.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		SelectionListener prioListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				Priority prio = (Priority) ((Button) e.widget).getData();
				setReminderPriority(prio);
			}
		};
		
		Button btnPrioLow = new Button(grpPriority, SWT.RADIO);
		btnPrioLow.setText(Messages.ReminderDetailDialog_btnPriorityLow_text);
		btnPrioLow.setData(Priority.LOW);
		btnPrioLow.addSelectionListener(prioListener);
		btnPriorities[0] = btnPrioLow;
		
		Button btnPrioMed = new Button(grpPriority, SWT.RADIO);
		btnPrioMed.setText(Messages.ReminderDetailDialog_btnPriorityMedium_text);
		btnPrioMed.setData(Priority.MEDIUM);
		btnPrioMed.addSelectionListener(prioListener);
		btnPriorities[1] = btnPrioMed;
		
		Button btnPrioHigh = new Button(grpPriority, SWT.RADIO);
		btnPrioHigh.setText(Messages.ReminderDetailDialog_btnPriorityHigh_text);
		btnPrioHigh.setData(Priority.HIGH);
		btnPrioHigh.addSelectionListener(prioListener);
		btnPriorities[2] = btnPrioHigh;
		
		Composite compositeAction = new Composite(compositeSettings, SWT.NONE);
		compositeAction.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		GridLayout gl_compositeAction = new GridLayout(2, false);
		gl_compositeAction.marginLeft = 2;
		gl_compositeAction.marginHeight = 0;
		gl_compositeAction.marginWidth = 0;
		compositeAction.setLayout(gl_compositeAction);
		
		Label labelAction = new Label(compositeAction, SWT.NONE);
		labelAction.setText(Messages.ReminderDetailDialog_labelAction_text);
		
		cvActionType = new ComboViewer(compositeAction, SWT.NONE);
		Combo comboActionType = cvActionType.getCombo();
		comboActionType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cvActionType.setContentProvider(ArrayContentProvider.getInstance());
		cvActionType.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				Type at = (Type) element;
				return at.getLocaleText();
			}
		});
		cvActionType.setInput(Type.values());
		
		Composite compositeDueDate = new Composite(compositeSettings, SWT.NONE);
		compositeDueDate.setLayout(new GridLayout(2, false));
		compositeDueDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		Label dueDate = new Label(compositeDueDate, SWT.CHECK);
		dueDate.setText(Messages.EditReminderDialog_dueOn);
		
		dateDuePicker = new DatePickerCombo(compositeDueDate, SWT.BORDER);
		dateDuePicker.setDate(new Date());
		dateDuePicker.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				DatePickerCombo dpc = (DatePickerCombo) e.widget;
				ProcessStatus curPs =
					(processStatus == ProcessStatus.DUE || processStatus == ProcessStatus.OVERDUE)
							? ProcessStatus.OPEN : processStatus;
				ProcessStatus newPs =
					Reminder.determineCurrentStatus(curPs, new TimeTool(dpc.getDate()));
				setReminderStatus(newPs);
			}
		});
		
		initialize();
		
		return area;
	}
	
	private void initialize(){
		List<Visibility> visVal = new ArrayList<Visibility>(Arrays.asList(Visibility.values()));
		
		if (reminder == null) {
			patient = ElexisEventDispatcher.getSelectedPatient();
			if (patient == null) {
				visVal.remove(Visibility.ON_PATIENT_SELECTION);
				visVal.remove(Visibility.POPUP_ON_PATIENT_SELECTION);
			}
			
			setReminderPriority(Priority.MEDIUM);
			setReminderStatus(ProcessStatus.OPEN);
			cvActionType.setSelection(new StructuredSelection(Type.COMMON));
		} else {
			patient = reminder.getKontakt();
			
			TimeTool dateDue = reminder.getDateDue();
			dateDuePicker.setDate(dateDue.getTime());
			
			txtSubject.setText(reminder.getSubject());
			txtDescription.setText(reminder.getMessage());
			setReminderPriority(reminder.getPriority());
			setReminderStatus(reminder.getStatus());
			cvActionType.setSelection(new StructuredSelection(reminder.getActionType()));
			lvResponsible.setSelection(new StructuredSelection(reminder.getResponsibles()));
			btnNotPatientRelated
				.setSelection(reminder.getCreator().getId().equals(reminder.getKontakt().getId()));
		}
		
		if (patient != null) {
			if (reminder != null && reminder.getCreator() != null
				&& patient.getId().equals(reminder.getCreator().getId())) {
				lblRelatedPatient.setText(Messages.EditReminderDialog_noPatient);
				visVal.remove(Visibility.ON_PATIENT_SELECTION);
				visVal.remove(Visibility.POPUP_ON_PATIENT_SELECTION);
			} else {
				lblRelatedPatient.setText(patient.getLabel());
				lblRelatedPatient.setBackground(SWTResourceManager.getColor(0, 0, 0));
				lblRelatedPatient.setForeground(SWTResourceManager.getColor(255, 255, 255));
			}
		} else {
			lblRelatedPatient.setText(Messages.EditReminderDialog_noPatientSelected);
		}
		
		cvVisibility.setInput(visVal);
		if (reminder == null) {
			cvVisibility.setSelection(new StructuredSelection(Visibility.ALWAYS));
		} else {
			cvVisibility.setSelection(new StructuredSelection(reminder.getVisibility()));
		}
	}
	
	private void setReminderPriority(Priority priority){
		this.priority = priority;
		for (int i = 0; i < btnPriorities.length; i++) {
			btnPriorities[i].setSelection(priority.numericValue() == i);
		}
	}
	
	private void setReminderStatus(ProcessStatus processStatus){
		this.processStatus = processStatus;
		for (int i = 0; i < btnProcessStatus.length; i++) {
			btnProcessStatus[i].setSelection(btnProcessStatus[i].getData() == processStatus);
		}
	}
	
	@Override
	protected void okPressed(){
		if (txtSubject.getText().length() > 2) {
			setErrorMessage(null);
			performOk();
		} else {
			setErrorMessage("Es muss ein Betreff gesetzt sein.");
		}
	}
	
	private void performOk(){
		String due = new TimeTool(dateDuePicker.getDate().getTime()).toString(TimeTool.DATE_GER);
		if (reminder == null) {
			reminder = new Reminder(null, due, Visibility.ALWAYS, "", "");
		}
		
		String contactId =
			(btnNotPatientRelated.getSelection()) ? CoreHub.actUser.getId() : patient.getId();
		Visibility visibility =
			(Visibility) ((StructuredSelection) cvVisibility.getSelection()).getFirstElement();
		Type atype =
			(Type) ((StructuredSelection) cvActionType.getSelection()).getFirstElement();
		if (atype == null) {
			atype = Type.COMMON;
		}
		
		String[] fields = new String[] {
			Reminder.FLD_SUBJECT, Reminder.MESSAGE, Reminder.FLD_PRIORITY, Reminder.FLD_STATUS,
			Reminder.KONTAKT_ID, Reminder.FLD_VISIBILITY, Reminder.DUE, Reminder.FLD_ACTION_TYPE
		};
		reminder.set(fields, txtSubject.getText(), txtDescription.getText(),
			Integer.toString(priority.numericValue()),
			Integer.toString(processStatus.numericValue()), contactId,
			Integer.toString(visibility.numericValue()), due,
			Integer.toString(atype.numericValue()));
		
		reminder.getResponsibles().stream().forEachOrdered(r -> reminder.removeResponsible(r));
		
		StructuredSelection ss = (StructuredSelection) lvResponsible.getSelection();
		@SuppressWarnings("unchecked")
		List<Object> selectionList = ss.toList();
		if (selectionList.contains(TX_ALL)) {
			CoreHub.getUserList().stream().forEachOrdered(c -> reminder.addResponsible(c));
		} else {
			selectionList.stream().map(e -> (Anwender) e)
				.forEachOrdered(c -> reminder.addResponsible(c));
		}
		
		super.okPressed();
	}
	
	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent){
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
	
}
