package ch.elexis.core.ui.dbcheck.contributions.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.data.BillingSystem;

public class RenameAccountingSysFieldDialog extends TitleAreaDialog {
	private Text txtNewName;
	private ComboViewer cViewerAccSys, cViewerField;
	
	private String[] accSystems;
	private String accountingSystem, presentFieldName, newFieldName;
	
	public RenameAccountingSysFieldDialog(Shell parentShell){
		super(parentShell);
		accSystems = BillingSystem.getAbrechnungsSysteme();
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		setTitle("Abrechnungssystem - Feld umbenennen");
		setMessage("Bitte Feld wählen und neuen Namen definieren");
		
		Composite container = (Composite) super.createDialogArea(parent);
		Composite area = new Composite(container, SWT.NONE);
		area.setLayoutData(new GridData(GridData.FILL_BOTH));
		area.setLayout(new GridLayout(2, false));
		
		Label lblAccountingsys = new Label(area, SWT.NONE);
		lblAccountingsys.setText("Abrechnungssystem");
		
		cViewerAccSys = new ComboViewer(area, SWT.READ_ONLY);
		Combo cmbAccSys = cViewerAccSys.getCombo();
		cmbAccSys.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cViewerAccSys.setContentProvider(ArrayContentProvider.getInstance());
		cViewerAccSys.setLabelProvider(new LabelProvider());
		cViewerAccSys.setInput(accSystems);
		cViewerAccSys.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (!selection.isEmpty()) {
					String selSystem = (String) selection.getFirstElement();
					List<String> availableFields = new ArrayList<String>();
					availableFields.addAll(parseNamelist(BillingSystem.getOptionals(selSystem)));
					availableFields.addAll(parseNamelist(BillingSystem.getRequirements(selSystem)));
					cViewerField.setInput(availableFields);
				}
			}
		});
		
		Label lblField = new Label(area, SWT.NONE);
		lblField.setText("Feld");
		
		cViewerField = new ComboViewer(area, SWT.READ_ONLY);
		Combo cmbField = cViewerField.getCombo();
		cmbField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cViewerField.setContentProvider(ArrayContentProvider.getInstance());
		cViewerField.setLabelProvider(new LabelProvider());
		cViewerField.setInput("");
		
		Label lblNewName = new Label(area, SWT.NONE);
		lblNewName.setText("Umbenennen zu");
		
		txtNewName = new Text(area, SWT.BORDER);
		txtNewName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		if (accSystems.length > 0) {
			cViewerAccSys.setSelection(new StructuredSelection(accSystems[0]));
		}
		return area;
	}
	
	private List<String> parseNamelist(String fields){
		List<String> fieldNames = new ArrayList<String>();
		if (fields == null || fields.isEmpty()) {
			return fieldNames;
		}
		
		String[] fieldArray = fields.split(";");
		for (String field : fieldArray) {
			String[] split = field.split(":");
			fieldNames.add(split[0]);
			System.out.println("added field name: " + split[0]);
		}
		return fieldNames;
	}
	
	@Override
	protected void okPressed(){
		IStructuredSelection selAccSys = (IStructuredSelection) cViewerAccSys.getSelection();
		if (!selAccSys.isEmpty()) {
			accountingSystem = (String) selAccSys.getFirstElement();
		}
		
		IStructuredSelection selField = (IStructuredSelection) cViewerField.getSelection();
		if (!selField.isEmpty()) {
			presentFieldName = (String) selField.getFirstElement();
		}
		
		newFieldName = txtNewName.getText();
		
		String[] fields = new String[] {
			accountingSystem, presentFieldName, newFieldName
		};
		
		if (validFields(fields)) {
			super.okPressed();
		} else {
			MessageDialog.openWarning(getParentShell(), "Unvollständig",
				"Bitte alle Felder auswählen bzw. ausfüllen");
		}
	}
	
	private boolean validFields(String[] fields){
		for (String field : fields) {
			if (field == null || field.isEmpty()) {
				return false;
			}
		}
		return true;
	}
	
	public String getAccountingSystem(){
		return accountingSystem;
	}
	
	public String getPresentFieldName(){
		return presentFieldName;
	}
	
	public String getNewFieldName(){
		return newFieldName;
	}
}
