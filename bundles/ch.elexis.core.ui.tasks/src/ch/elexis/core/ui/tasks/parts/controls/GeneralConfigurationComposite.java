package ch.elexis.core.ui.tasks.parts.controls;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import ch.elexis.core.model.IUser;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.OwnerTaskNotification;

public class GeneralConfigurationComposite extends AbstractTaskDescriptorConfigurationComposite {
	
	private Text txtReferenceId;
	private Text txtOwnerId;
	private Text txtRunner;
	private ComboViewer cvNotificationType;
	private Button btnSingleton;
	private Button btnActive;
	
	public GeneralConfigurationComposite(Composite parent, int style){
		super(parent, style);
		
		setLayout(new GridLayout(2, false));
		
		Label lblReferenceid = new Label(this, SWT.NONE);
		lblReferenceid.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblReferenceid.setText("referenceId");
		
		txtReferenceId = new Text(this, SWT.BORDER);
		txtReferenceId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtReferenceId.addModifyListener(event -> {
			String text = ((Text) event.widget).getText();
			if (taskDescriptor != null) {
				taskDescriptor.setReferenceId(text);
				saveTaskDescriptor();
				// TODO catch exception if reference id is not unique
			}
		});
		
		Label lblOwner = new Label(this, SWT.NONE);
		lblOwner.setText("owner");
		
		Composite compOwner = new Composite(this, SWT.NONE);
		GridLayout gl_compOwner = new GridLayout(2, false);
		gl_compOwner.verticalSpacing = 0;
		gl_compOwner.marginWidth = 0;
		gl_compOwner.marginHeight = 0;
		compOwner.setLayout(gl_compOwner);
		compOwner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		txtOwnerId = new Text(compOwner, SWT.BORDER);
		txtOwnerId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtOwnerId.setBounds(0, 0, 64, 19);
		
		Link linkOwner = new Link(compOwner, SWT.NONE);
		linkOwner.setText("<a>...</a>");
		linkOwner.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e){
				openUserSelectionDialog();
			}
			
		});
		
		Label lblNotification = new Label(this, SWT.NONE);
		lblNotification.setText("notification");
		
		cvNotificationType = new ComboViewer(this, SWT.NONE);
		Combo combo = cvNotificationType.getCombo();
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cvNotificationType.setContentProvider(ArrayContentProvider.getInstance());
		cvNotificationType.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				if (element == null) {
					return "";
				}
				OwnerTaskNotification otn = (OwnerTaskNotification) element;
				switch (otn) {
				case NEVER:
					return "Notify never";
				case WHEN_FINISHED:
					return "Notify when task finished";
				case WHEN_FINISHED_FAILED:
					return "Notify when task failed";
				default:
					break;
				}
				return super.getText(element);
			}
		});
		cvNotificationType.setInput(OwnerTaskNotification.values());
		cvNotificationType.addSelectionChangedListener(sel -> {
			OwnerTaskNotification otn =
				(OwnerTaskNotification) sel.getStructuredSelection().getFirstElement();
			if(taskDescriptor != null) {
				taskDescriptor.setOwnerNotification(otn);
				saveTaskDescriptor();
			}
		});
		
		Label lblRunner = new Label(this, SWT.NONE);
		lblRunner.setText("runner");
		
		txtRunner = new Text(this, SWT.BORDER);
		txtRunner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtRunner.setToolTipText(
			"The station to run on (e.g. ELEXIS-SERVER), leave empty for all stations");
		txtRunner.addModifyListener(ev -> {
			String runner = ((Text) ev.widget).getText();
			if(taskDescriptor != null) {
				taskDescriptor.setRunner(runner);
				saveTaskDescriptor();
			}

		});
		
		new Label(this, SWT.NONE);
		btnSingleton = new Button(this, SWT.CHECK);
		btnSingleton.setText("singleton");
		btnSingleton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				boolean selection = ((Button) e.widget).getSelection();
				if(taskDescriptor != null) {
					taskDescriptor.setSingleton(selection);
					saveTaskDescriptor();
				}
			}
		});
		
		new Label(this, SWT.NONE);
		btnActive = new Button(this, SWT.CHECK);
		btnActive.setText("active");
		btnSingleton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				boolean selection = ((Button) e.widget).getSelection();
				if(taskDescriptor != null) {
					taskDescriptor.setActive(selection);
					saveTaskDescriptor();
				}
			}
		});
	}
	
	@Override
	public void setSelection(ITaskDescriptor taskDescriptor){
		super.setSelection(taskDescriptor);
		
		if (taskDescriptor != null) {
			txtReferenceId.setText(
				taskDescriptor.getReferenceId() != null ? taskDescriptor.getReferenceId() : "");
			String ownerId =
				taskDescriptor.getOwner() != null ? taskDescriptor.getOwner().getId() : "";
			txtOwnerId.setText(ownerId);
			txtRunner.setText(taskDescriptor.getRunner());
			cvNotificationType
				.setSelection(new StructuredSelection(taskDescriptor.getOwnerNotification()));
			btnActive.setSelection(taskDescriptor.isActive());
			btnSingleton.setSelection(taskDescriptor.isSingleton());
		} else {
			txtReferenceId.setText("");
			txtOwnerId.setText("");
			txtRunner.setText("");
			cvNotificationType.setSelection(null);
			btnSingleton.setSelection(false);
			btnActive.setSelection(false);
		}
		
	}
	
	private void openUserSelectionDialog(){
		ListDialog listDialog = new ListDialog(getShell());
		listDialog.setContentProvider(ArrayContentProvider.getInstance());
		List<IUser> users = CoreModelServiceHolder.get().getQuery(IUser.class).execute();
		Collections.sort(users, Comparator.comparing(IUser::getId));
		listDialog.setInput(users);
		listDialog.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				IUser runnable = (IUser) element;
				return runnable.getId();
			}
		});
		listDialog.setMessage("Select an action");
		int open = listDialog.open();
		if (open == ListSelectionDialog.OK) {
			Object[] result = listDialog.getResult();
			if (result != null && result.length >= 1) {
				IUser selection = (IUser) result[0];
				taskDescriptor.setOwner(selection);
				setSelection(taskDescriptor);
			}
		}
	}
	
}
