package ch.elexis.core.ui.propertypage;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.locks.IUnlockable;
import ch.elexis.data.Patient;

public class PatientAddressPage extends PropertyPage implements IWorkbenchPropertyPage, IUnlockable {
	
	private Patient pat;
	private Text textStrasse;
	private Text textPostleitzahl;
	private Text textOrtschaft;
	private TableCombo tableCombo;
	
	public PatientAddressPage(){}
	
	@Override
	protected Control createContents(Composite parent){
		init();
		Composite comp = new Composite(parent, SWT.None);
		comp.setLayout(new GridLayout(2, false));
		
		Label lblNewLabel = new Label(comp, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("Strasse");
		
		textStrasse = new Text(comp, SWT.BORDER);
		textStrasse.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textStrasse.setTextLimit(80);
		
		Label lblPostleitzahl = new Label(comp, SWT.NONE);
		lblPostleitzahl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPostleitzahl.setText("Postleitzahl");
		
		textPostleitzahl = new Text(comp, SWT.BORDER);
		textPostleitzahl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textPostleitzahl.setTextLimit(6);
		
		Label lblOrtschaft = new Label(comp, SWT.NONE);
		lblOrtschaft.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblOrtschaft.setText("Ortschaft");
		
		textOrtschaft = new Text(comp, SWT.BORDER);
		textOrtschaft.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textOrtschaft.setTextLimit(50);
		
		Label lblLand = new Label(comp, SWT.NONE);
		lblLand.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblLand.setText("Land");
		
		TableComboViewer countryComboViewer = new TableComboViewer(comp);
		tableCombo = countryComboViewer.getTableCombo();
		tableCombo.setTableWidthPercentage(90);
		tableCombo.setShowFontWithinSelection(false);
		tableCombo.setShowColorWithinSelection(false);
		tableCombo.setShowTableLines(false);
		tableCombo.setShowTableHeader(false);
		tableCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		countryComboViewer.setLabelProvider(new CountryComboLabelProvider());
		countryComboViewer.setContentProvider(new ArrayContentProvider());
		String[] items = new String[] {
			"CH", "FL", "AT", "DE", "FR", "IT"
		};
		countryComboViewer.setInput(items);
		
		super.setTitle(pat.getLabel());
		textStrasse.setText(pat.get(Patient.FLD_STREET));
		textPostleitzahl.setText(pat.get(Patient.FLD_ZIP));
		textOrtschaft.setText(pat.get(Patient.FLD_PLACE));
		String country = pat.get(Patient.FLD_COUNTRY).trim();
		for (int i = 0; i < items.length; i++) {
			if (country.equalsIgnoreCase(items[i])) {
				countryComboViewer.setSelection(new StructuredSelection(items[i]), true);
			}
		}
		
		setUnlocked(CoreHub.getLocalLockService().isLocked(pat));
		
		return comp;
	}
	
	private void init(){
		IAdaptable adapt = getElement();
		pat = (Patient) adapt.getAdapter(Patient.class);
	}
	
	@Override
	protected void performApply(){
		String[] fields = {
			Patient.FLD_STREET, Patient.FLD_COUNTRY, Patient.FLD_ZIP, Patient.FLD_PLACE
		};
		String[] values =
			{
				textStrasse.getText(), tableCombo.getText(), textPostleitzahl.getText(),
				textOrtschaft.getText()
			};
		pat.set(fields, values);
	}
	
	@Override
	public boolean performOk(){
		performApply();
		return true;
	}

	@Override
	public void setUnlocked(boolean unlocked){
		textStrasse.setEditable(unlocked);
		textPostleitzahl.setEditable(unlocked);
		textOrtschaft.setEditable(unlocked);
		tableCombo.setEnabled(unlocked);
	}
}
