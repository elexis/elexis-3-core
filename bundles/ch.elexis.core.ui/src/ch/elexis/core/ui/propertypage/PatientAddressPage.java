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

import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.types.Country;
import ch.elexis.core.ui.locks.IUnlockable;

public class PatientAddressPage extends PropertyPage implements IWorkbenchPropertyPage, IUnlockable {
	
	private IPatient pat;
	private Text textStrasse;
	private Text textPostleitzahl;
	private Text textOrtschaft;
	private TableComboViewer countryComboViewer;
	
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
		
		countryComboViewer = new TableComboViewer(comp);
		TableCombo tableCombo = countryComboViewer.getTableCombo();
		tableCombo.setTableWidthPercentage(90);
		tableCombo.setShowFontWithinSelection(false);
		tableCombo.setShowColorWithinSelection(false);
		tableCombo.setShowTableLines(false);
		tableCombo.setShowTableHeader(false);
		tableCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		countryComboViewer.setLabelProvider(new CountryComboLabelProvider());
		countryComboViewer.setContentProvider(new ArrayContentProvider());
		Country[] items = new Country[] {
			Country.CH, Country.LI, Country.AT, Country.DE, Country.FR, Country.IT
		};
		countryComboViewer.setInput(items);
		
		super.setTitle(pat.getLabel());
		textStrasse.setText(pat.getStreet());
		textPostleitzahl.setText(pat.getZip());
		textOrtschaft.setText(pat.getCity());
		countryComboViewer.setSelection(new StructuredSelection(pat.getCountry()));
		
		setUnlocked(LocalLockServiceHolder.get().isLocked(pat));
		
		return comp;
	}
	
	private void init(){
		IAdaptable adapt = getElement();
		pat = (IPatient) adapt.getAdapter(IPatient.class);
	}
	
	@Override
	protected void performApply(){
		pat.setStreet(textStrasse.getText());
		StructuredSelection countrySel = (StructuredSelection) countryComboViewer.getSelection();
		if (!countrySel.isEmpty()) {
			pat.setCountry((Country) countrySel.getFirstElement());
		}
		pat.setZip(textPostleitzahl.getText());
		pat.setCity(textOrtschaft.getText());
		CoreModelServiceHolder.get().save(pat);
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
		countryComboViewer.getControl().setEnabled(unlocked);
	}
}
