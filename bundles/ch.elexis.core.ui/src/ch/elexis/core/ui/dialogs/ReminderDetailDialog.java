package ch.elexis.core.ui.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
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
import ch.elexis.core.model.issue.Priority;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.model.issue.Type;
import ch.elexis.core.model.issue.Visibility;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.dialogs.controls.ReminderVisibilityAndPopupComposite;
import ch.elexis.core.ui.icons.ImageSize;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.data.Anwender;
import ch.elexis.data.Patient;
import ch.elexis.data.Reminder;
import ch.rgw.tools.TimeTool;

public class ReminderDetailDialog extends TitleAreaDialog {
	
	private static final String TX_ALL = Messages.EditReminderDialog_all; //$NON-NLS-1$
	
	private Reminder reminder = null;
	private Patient patient = null;
	
	private Priority priority = Priority.MEDIUM;
	private ProcessStatus processStatus = ProcessStatus.OPEN;
	private Type actionType = Type.COMMON;
	private TimeTool dateDue = null;
	@SuppressWarnings("rawtypes")
	private List responsibles = Collections.singletonList(CoreHub.actUser);
	
	private Text txtSubject;
	private Text txtDescription;
	private DatePickerCombo dateDuePicker;
	private Label lblRelatedPatient;
	private Button[] btnProcessStatus = new Button[4];
	private ComboViewer cvActionType;
	private Button btnNotPatientRelated;
	private ListViewer lvResponsible;
	private ReminderVisibilityAndPopupComposite rvapc;
	private ComboViewer cvPriority;
	private Button btnHasDueDate;
	private Composite dueComposite;
	
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
		lvResponsible.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2){
				if (e1 instanceof String) {
					// to pin TX_ALL to the list head
					return -1;
				}
				if (e2 instanceof String) {
					// to pin TX_ALL to the list head
					return 1;
				}
				String label1 = ((Anwender) e1).getLabel();
				String label2 = ((Anwender) e2).getLabel();
				return label1.toLowerCase().compareTo(label2.toLowerCase());
			}
		});
		lvResponsible.addSelectionChangedListener(sc -> {
			responsibles = ((StructuredSelection) sc.getSelection()).toList();
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
		btnNotPatientRelated.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				if (btnNotPatientRelated.getSelection()) {
					lblRelatedPatient.setText(Messages.EditReminderDialog_noPatient);
					patient = null;
				} else {
					patient = ElexisEventDispatcher.getSelectedPatient();
					if (patient == null) {
						lblRelatedPatient.setText(Messages.EditReminderDialog_noPatientSelected);
						btnNotPatientRelated.setSelection(true);
					}
				}
			
				rvapc.setConfiguredVisibility(null, patient != null);
			
				updateModelToTarget();
				super.widgetSelected(e);
			}
		});
		
		txtSubject = new Text(compositeMessage, SWT.BORDER);
		txtSubject.setMessage(Messages.ReminderDetailDialog_txtSubject_message);
		txtSubject.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtSubject.setTextLimit(160);
		txtSubject.setFocus();
		
		txtDescription = new Text(compositeMessage, SWT.BORDER | SWT.WRAP);
		txtDescription.setMessage(Messages.ReminderDetailDialog_txtDescription_message);
		txtDescription.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		txtDescription.setBounds(0, 0, 64, 19);
		
		Composite composite = new Composite(compositeMessage, SWT.NONE);
		GridLayout gl_composite = new GridLayout(2, false);
		gl_composite.verticalSpacing = 0;
		gl_composite.marginWidth = 0;
		gl_composite.marginHeight = 0;
		composite.setLayout(gl_composite);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		rvapc = new ReminderVisibilityAndPopupComposite(composite, SWT.NONE);
		rvapc.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(composite, SWT.NONE);
		
		Composite compositeState = new Group(container, SWT.SHADOW_NONE);
		compositeState.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		RowLayout rl_compState = new RowLayout(SWT.HORIZONTAL);
		rl_compState.marginTop = 0;
		rl_compState.marginBottom = 0;
		compositeState.setLayout(rl_compState);
		
		SelectionListener processStatusListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				processStatus = (ProcessStatus) ((Button) e.widget).getData();
			}
		};
		
		Button btnOpen = new Button(compositeState, SWT.RADIO);
		btnOpen.setText(ProcessStatus.OPEN.getLocaleText());
		btnOpen.setData(ProcessStatus.OPEN);
		btnOpen.addSelectionListener(processStatusListener);
		btnProcessStatus[0] = btnOpen;
		
		Button btnInProgress = new Button(compositeState, SWT.RADIO);
		btnInProgress.setText(ProcessStatus.IN_PROGRESS.getLocaleText());
		btnInProgress.setData(ProcessStatus.IN_PROGRESS);
		btnInProgress.addSelectionListener(processStatusListener);
		btnProcessStatus[1] = btnInProgress;
		
		Button btnClosed = new Button(compositeState, SWT.RADIO);
		btnClosed.setText(ProcessStatus.CLOSED.getLocaleText());
		btnClosed.setData(ProcessStatus.CLOSED);
		btnClosed.addSelectionListener(processStatusListener);
		btnProcessStatus[2] = btnClosed;
		
		Button btnOnHold = new Button(compositeState, SWT.RADIO);
		btnOnHold.setText(ProcessStatus.ON_HOLD.getLocaleText());
		btnOnHold.setData(ProcessStatus.ON_HOLD);
		btnOnHold.addSelectionListener(processStatusListener);
		btnProcessStatus[3] = btnOnHold;
		
		Label separator = new Label(compositeState, SWT.SEPARATOR | SWT.VERTICAL);
		separator.setLayoutData(new RowData(25, 20));
		
		dueComposite = new Composite(compositeState, SWT.NONE);
		GridLayout gl_dueComposite = new GridLayout(2, false);
		gl_dueComposite.horizontalSpacing = 0;
		gl_dueComposite.marginWidth = 0;
		gl_dueComposite.verticalSpacing = 0;
		gl_dueComposite.marginHeight = 0;
		dueComposite.setLayout(gl_dueComposite);
		
		btnHasDueDate = new Button(dueComposite, SWT.CHECK);
		btnHasDueDate.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnHasDueDate.setText(Messages.EditReminderDialog_dueOn);
		btnHasDueDate.setSelection(false);
		btnHasDueDate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				if (btnHasDueDate.getSelection()) {
					dateDue = new TimeTool();
					dateDuePicker.setDate(dateDue.getTime());
				} else {
					dateDuePicker.setDate(null);
					dateDue = null;
				}
				dateDuePicker.setEnabled(btnHasDueDate.getSelection());
				updateModelToTarget();
			}
		});
		
		dateDuePicker = new DatePickerCombo(dueComposite, SWT.BORDER);
		dateDuePicker.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		dateDuePicker.setEnabled(false);
		dateDuePicker.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				dateDue = new TimeTool(dateDuePicker.getDate().getTime());
			}
		});
		
		Label separator2 = new Label(compositeState, SWT.SEPARATOR | SWT.VERTICAL);
		separator2.setLayoutData(new RowData(25, 20));
		
		cvPriority = new ComboViewer(compositeState, SWT.SINGLE);
		cvPriority.getCombo().setLayoutData(new RowData(50, SWT.DEFAULT));
		cvPriority.setContentProvider(ArrayContentProvider.getInstance());
		cvPriority.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				Priority prio = (Priority) element;
				return prio.getLocaleText();
			}
		});
		cvPriority.setInput(Priority.values());
		cvPriority.addSelectionChangedListener(s -> {
			priority = (Priority) ((StructuredSelection) s.getSelection()).getFirstElement();
		});
		
		Composite compositeSettings = new Composite(container, SWT.BORDER);
		compositeSettings.setLayout(new GridLayout(2, false));
		compositeSettings.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		Label labelAction = new Label(compositeSettings, SWT.NONE);
		labelAction.setText(Messages.ReminderDetailDialog_labelAction_text);
		
		cvActionType = new ComboViewer(compositeSettings, SWT.NONE);
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
		cvActionType.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				actionType = (Type) ((StructuredSelection) event.getSelection()).getFirstElement();
			}
		});
		
		initialize();
		
		return area;
	}
	
	private void initialize(){
		if (reminder == null) {
			patient = ElexisEventDispatcher.getSelectedPatient();
		} else {
			patient = reminder.getKontakt();
			dateDue = reminder.getDateDue();
			priority = reminder.getPriority();
			processStatus = reminder.getProcessStatus();
			if (ProcessStatus.DUE == processStatus || ProcessStatus.OVERDUE == processStatus) {
				processStatus = ProcessStatus.OPEN;
				if (dateDue == null) {
					dateDue = new TimeTool();
					dateDue.addDays(-14);
				}
			}
			actionType = reminder.getActionType();
			
			String[] strings = reminder.get(false, Reminder.FLD_SUBJECT, Reminder.FLD_MESSAGE);
			if (strings[0].length() == 0 && strings[1].length() > 0) {
				txtSubject.setText(strings[1]);
			} else {
				txtSubject.setText(reminder.getSubject());
			}
			txtDescription.setText(strings[1]);
			
			btnNotPatientRelated.setSelection(!reminder.isPatientRelated());
		}
		if (reminder != null) {
			rvapc.setConfiguredVisibility(reminder.getVisibility(),
				reminder.isPatientRelated() && patient != null);
		} else {
			rvapc.setConfiguredVisibility(null, patient != null);
		}
		updateModelToTarget();
	}
	
	private void updateModelToTarget(){
		if(reminder != null) {
			List<Anwender> resp = reminder.getResponsibles();
			if (resp == null) {
				responsibles = Collections.singletonList(TX_ALL);
			} else {
				responsibles = resp;
			}
		}

		lvResponsible.setSelection(new StructuredSelection(responsibles));
		cvActionType.setSelection(new StructuredSelection(actionType));
		cvPriority.setSelection(new StructuredSelection(priority));
		
		dateDuePicker.setEnabled(dateDue != null);
		dateDuePicker.setDate((dateDue != null) ? dateDue.getTime() : null);
		btnHasDueDate.setSelection(dateDue != null);
		
		if (patient != null) {
			if (reminder != null && reminder.getCreator() != null
				&& patient.getId().equals(reminder.getCreator().getId())) {
				lblRelatedPatient.setText(Messages.EditReminderDialog_noPatient);
			} else {
				lblRelatedPatient.setText(patient.getLabel());
				lblRelatedPatient.setBackground(SWTResourceManager.getColor(0, 0, 0));
				lblRelatedPatient.setForeground(SWTResourceManager.getColor(255, 255, 255));
			}
		}
		btnNotPatientRelated.setSelection(patient == null);
		
		int dueState = Reminder.determineDueState(dateDue);
		if (dueState > 0) {
			btnHasDueDate.setBackground(UiDesk.getColor(UiDesk.COL_RED));
		} else {
			btnHasDueDate.setBackground(null);
		}
		
		for (int i = 0; i < btnProcessStatus.length; i++) {
			btnProcessStatus[i].setSelection(btnProcessStatus[i].getData() == processStatus);
			if (dueState > 0) {
				if (btnProcessStatus[i].getData() == processStatus) {
					btnProcessStatus[i].setForeground(UiDesk.getColor(UiDesk.COL_RED));
				} else {
					btnProcessStatus[i].setForeground(UiDesk.getColor(UiDesk.COL_BLACK));
				}
			} else {
				btnProcessStatus[i].setForeground(UiDesk.getColor(UiDesk.COL_BLACK));
			}
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
		String due = null;
		if (btnHasDueDate.getSelection()) {
			due = dateDue.toString(TimeTool.DATE_GER);
		}
		if (reminder == null) {
			reminder = new Reminder(null, due, Visibility.ALWAYS, "", "");
		}
		
		String contactId =
			(btnNotPatientRelated.getSelection()) ? reminder.getCreator().getId() : patient.getId();
		Visibility visibility = rvapc.getConfiguredVisibility();
		
		String[] fields = new String[] {
			Reminder.FLD_SUBJECT, Reminder.FLD_MESSAGE, Reminder.FLD_PRIORITY, Reminder.FLD_STATUS,
			Reminder.FLD_KONTAKT_ID, Reminder.FLD_VISIBILITY, Reminder.FLD_DUE, Reminder.FLD_ACTION_TYPE
		};
		reminder.set(fields, txtSubject.getText(), txtDescription.getText(),
			Integer.toString(priority.numericValue()),
			Integer.toString(processStatus.numericValue()), contactId,
			Integer.toString(visibility.numericValue()), due,
			Integer.toString(actionType.numericValue()));
		
		StructuredSelection ss = (StructuredSelection) lvResponsible.getSelection();
		@SuppressWarnings("unchecked")
		List<Object> selectionList = ss.toList();
		if (selectionList.contains(TX_ALL)) {
			reminder.setResponsible(null);
		} else {
			reminder.setResponsible(
				selectionList.stream().map(e -> (Anwender) e).collect(Collectors.toList()));
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
	
	public Reminder getReminder(){
		return reminder;
	}
	
}
