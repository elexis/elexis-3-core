package ch.elexis.core.ui.dbcheck.contributions.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.data.Patient;

public class SelectValueAccountingSysFieldDialog extends TitleAreaDialog {
	private Patient patient;
	private String accountingSys;
	private String oldField;
	private String newField;
	private String oldValue;
	private String newValue;
	
	private Button btnRememberProceedure, btnOldValue, btnNewValue;
	
	private boolean useLegacyValue;
	private boolean rememberProceedure;
	
	public SelectValueAccountingSysFieldDialog(Shell parentShell, Patient patient,
		String accountingSys, String oldField, String oldValue, String newField, String newValue){
		super(parentShell);
		this.patient = patient;
		this.accountingSys = accountingSys;
		this.oldField = oldField;
		this.oldValue = oldValue;
		this.newField = newField;
		this.newValue = newValue;
		
		rememberProceedure = false;
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		setTitle(accountingSys + " Feld umbenennen");
		setMessage("Einträge für neuen und alten Feldnamen gefunden.\nWelcher Wert soll für '"
			+ newField + "' gespeichert werden?");
		
		Composite container = (Composite) super.createDialogArea(parent);
		Composite area = new Composite(container, SWT.NONE);
		area.setLayoutData(new GridData(GridData.FILL_BOTH));
		area.setLayout(new GridLayout(1, false));
		
		Group group = new Group(area, SWT.NONE);
		group.setLayout(new GridLayout(1, false));
		group.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label lblInfo = new Label(group, SWT.NONE);
		lblInfo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblInfo.setText("Betroffener Patient: " + patient.getLabel());
		
		Label lblOldField = new Label(group, SWT.NONE);
		lblOldField.setText("Altes Feld - " + oldField);
		btnOldValue = new Button(group, SWT.RADIO);
		btnOldValue.setText("Wert: " + oldValue);
		
		Label label = new Label(group, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNewField = new Label(group, SWT.NONE);
		lblNewField.setText("Neues Feld - " + newField);
		btnNewValue = new Button(group, SWT.RADIO);
		btnNewValue.setText("Wert: " + newValue);
		
		btnRememberProceedure = new Button(area, SWT.CHECK);
		btnRememberProceedure.setText("Auswahl für weitere Konflikte merken");
		
		return area;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent){
		createButton(parent, IDialogConstants.OK_ID, "OK", false);
	}
	
	@Override
	protected void okPressed(){
		useLegacyValue = btnOldValue.getSelection();
		rememberProceedure = btnRememberProceedure.getSelection();
		super.okPressed();
	}
	
	public boolean rememberProceedure(){
		return rememberProceedure;
	}
	
	public boolean useLegacyValue(){
		return useLegacyValue;
	}
}
