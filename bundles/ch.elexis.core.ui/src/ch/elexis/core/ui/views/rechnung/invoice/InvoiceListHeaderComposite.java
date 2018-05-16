package ch.elexis.core.ui.views.rechnung.invoice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;

import ch.elexis.core.model.InvoiceState;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.views.rechnung.InvoiceListView;
import ch.elexis.core.ui.views.rechnung.Messages;
import ch.elexis.data.BillingSystem;
import ch.elexis.data.Patient;

public class InvoiceListHeaderComposite extends Composite {
	
	private final static String ALL_PATIENTS_LABEL = Messages.RnControlFieldProvider_allPatients;
	private final static String ALL_ELEMENTS_LABEL = Messages.RnControlFieldProvider_all;
	
	private Text txtInvoiceno;
	private Text txtAmount;
	private Patient actPatient;
	
	private Link linkPatient;
	private ComboViewer comboViewerStatus;
	private Label lblPatientname;
	private ComboViewer comboViewerType;
	private Button btnLimit;
	private ComboViewer comboViewerBillingSystem;
	
	private Color defaultBackgroundColor;
	private Label lblLimitWarn;
	
	/**
	 * 
	 * @param parent
	 * @param style
	 * @param invoiceListView
	 */
	public InvoiceListHeaderComposite(Composite parent, int style, InvoiceListView invoiceListView){
		super(parent, style);
		
		setLayout(new GridLayout(8, false));
		setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		new Label(this, SWT.NONE);
		
		Label lblStatus = new Label(this, SWT.NONE);
		lblStatus.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lblStatus.setText(Messages.InvoiceListView_tblclmnInvoiceState_text);
		
		HyperlinkAdapter hlPatient = new HyperlinkAdapter() {
			@Override
			public void linkActivated(final HyperlinkEvent e){
				Patient oldPatient = actPatient;
				KontaktSelektor ksl = new KontaktSelektor(parent.getShell(), Patient.class,
					Messages.RnControlFieldProvider_selectPatientCaption, //$NON-NLS-1$
					Messages.RnControlFieldProvider_selectPatientMessage, true); //$NON-NLS-1$
				if (ksl.open() == Dialog.OK) {
					actPatient = (Patient) ksl.getSelection();
					if (actPatient != null) {
						lblPatientname.setText(actPatient.getLabel());
						comboViewerStatus.setSelection(new StructuredSelection(ALL_ELEMENTS_LABEL));
					} else {
						lblPatientname.setText(ALL_PATIENTS_LABEL);
						comboViewerStatus.setSelection(new StructuredSelection(InvoiceState.OPEN));
					}
				} else {
					actPatient = null;
					lblPatientname.setText(ALL_PATIENTS_LABEL);
					comboViewerStatus.setSelection(new StructuredSelection(InvoiceState.OPEN));
				}
				
				if (actPatient == null && oldPatient == null) {
					return;
				} else if (actPatient != null && oldPatient != null
					&& actPatient.equals(oldPatient)) {
					return;
				}
				invoiceListView.refresh();
			}
		};
		
		linkPatient = new Link(this, SWT.NONE);
		linkPatient.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		linkPatient.setText(Messages.RnControlFieldProvider_patient2);
		linkPatient.setForeground(UiDesk.getColorRegistry().get(UiDesk.COL_BLUE));
		linkPatient.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(final MouseEvent e){
				if (hlPatient != null) {
					hlPatient.linkActivated(new HyperlinkEvent(linkPatient, linkPatient,
						linkPatient.getText(), e.stateMask));
				}
			}
			
		});
		
		Label lblBillingSystem = new Label(this, SWT.NONE);
		lblBillingSystem.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lblBillingSystem.setText(Messages.RnControlFieldProvider_PaymentSystem);
		
		Label lblType = new Label(this, SWT.NONE);
		lblType.setText(Messages.InvoiceListHeaderComposite_lblType_text);
		lblType.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		
		Label lblInvoiceno = new Label(this, SWT.NONE);
		lblInvoiceno.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lblInvoiceno.setText(Messages.InvoiceListView_tblclmnInvoiceNo_text);
		
		Label lblAmount = new Label(this, SWT.NONE);
		lblAmount.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lblAmount.setText(Messages.InvoiceListHeaderComposite_txtRgTotal);
		lblAmount.setToolTipText(Messages.InvoiceListHeaderComposite_txtAmount_toolTipText);
		
		lblLimitWarn = new Label(this, SWT.NONE);
		lblLimitWarn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblLimitWarn.setText("");
		defaultBackgroundColor = lblLimitWarn.getBackground();
		
		Label btnClear = new Label(this, SWT.FLAT);
		btnClear.setImage(Images.IMG_CLEAR.getImage());
		btnClear.setToolTipText(ch.elexis.core.ui.selectors.Messages.SelectorPanel_clearFields);
		btnClear.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e){
				clearValues();
				invoiceListView.refresh();
			}
		});
		
		comboViewerStatus = new ComboViewer(this, SWT.SINGLE | SWT.READ_ONLY);
		Combo comboStatus = comboViewerStatus.getCombo();
		GridData gd_comboStatus = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_comboStatus.widthHint = 140;
		comboStatus.setLayoutData(gd_comboStatus);
		comboViewerStatus.setContentProvider(ArrayContentProvider.getInstance());
		comboViewerStatus.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof InvoiceState) {
					InvoiceState invoiceState = (InvoiceState) element;
					return invoiceState.getLocaleText();
				}
				return super.getText(element);
			}
		});
		List<Object> values = new ArrayList<>();
		values.add(ALL_ELEMENTS_LABEL);
		values.add(InvoiceState.OPEN);
		values.add(InvoiceState.OPEN_AND_PRINTED);
		values.add(InvoiceState.PARTIAL_PAYMENT);
		values.add(InvoiceState.PAID);
		values.add(InvoiceState.EXCESSIVE_PAYMENT);
		values.add(InvoiceState.DEMAND_NOTE_1);
		values.add(InvoiceState.DEMAND_NOTE_1_PRINTED);
		values.add(InvoiceState.DEMAND_NOTE_2);
		values.add(InvoiceState.DEMAND_NOTE_2_PRINTED);
		values.add(InvoiceState.DEMAND_NOTE_3);
		values.add(InvoiceState.DEMAND_NOTE_3_PRINTED);
		values.add(InvoiceState.IN_EXECUTION);
		values.add(InvoiceState.PARTIAL_LOSS);
		values.add(InvoiceState.TOTAL_LOSS);
		values.add(InvoiceState.CANCELLED);
		values.add(InvoiceState.DEFECTIVE);
		values.add(InvoiceState.TO_PRINT);
		values.add(InvoiceState.OWING);
		values.add(InvoiceState.STOP_LEGAL_PROCEEDING);
		values.add(InvoiceState.DEPRECIATED);
		values.add(InvoiceState.REJECTED);
		comboViewerStatus.setInput(values);
		comboViewerStatus.setSelection(new StructuredSelection(InvoiceState.OPEN));
		comboViewerStatus.addSelectionChangedListener((event) ->  invoiceListView.refresh());
		
		lblPatientname = new Label(this, SWT.NONE);
		GridData gd_lblPatientname = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_lblPatientname.minimumWidth = 130;
		lblPatientname.setLayoutData(gd_lblPatientname);
		lblPatientname.setText(ALL_PATIENTS_LABEL);
		
		comboViewerBillingSystem = new ComboViewer(this, SWT.SINGLE | SWT.READ_ONLY);
		comboViewerBillingSystem.setContentProvider(ArrayContentProvider.getInstance());
		comboViewerBillingSystem.setLabelProvider(new LabelProvider());
		List<String> billingSystems = new ArrayList<String>();
		billingSystems.add(ALL_ELEMENTS_LABEL);
		billingSystems.addAll(Arrays.asList(getBillingSystems()));
		comboViewerBillingSystem.setInput(billingSystems);
		comboViewerBillingSystem.setSelection(new StructuredSelection(ALL_ELEMENTS_LABEL));
		comboViewerBillingSystem.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		comboViewerBillingSystem.addSelectionChangedListener((event) ->  invoiceListView.refresh());
		
		comboViewerType = new ComboViewer(this, SWT.SINGLE | SWT.READ_ONLY);
		comboViewerType.setContentProvider(ArrayContentProvider.getInstance());
		comboViewerType.setLabelProvider(new LabelProvider());
		comboViewerType.setInput(Arrays.asList(new String[]{ALL_ELEMENTS_LABEL, "TG", "TP"}));
		comboViewerType.setSelection(new StructuredSelection(ALL_ELEMENTS_LABEL));
	
		comboViewerType.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		comboViewerType.addSelectionChangedListener((event) ->  invoiceListView.refresh());
		
		txtInvoiceno = new Text(this, SWT.BORDER);
		txtInvoiceno.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtInvoiceno.addListener(SWT.Traverse, new Listener() {
			
			@Override
			public void handleEvent(Event event){
				if (event.detail == SWT.TRAVERSE_RETURN) {
					comboViewerStatus.setSelection(new StructuredSelection(ALL_ELEMENTS_LABEL));
					// execution will be started via comboViewerStatus#sclistener
				}
			}
		});
		
		txtAmount = new Text(this, SWT.BORDER);
		txtAmount.setToolTipText(Messages.InvoiceListHeaderComposite_txtAmount_toolTipText);
		txtAmount.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtAmount.addListener(SWT.Traverse, new Listener() {
			@Override
			public void handleEvent(Event event){
				if (event.detail == SWT.TRAVERSE_RETURN) {
					invoiceListView.refresh();
				}
			}
		});
		
		btnLimit = new Button(this, SWT.FLAT | SWT.TOGGLE | SWT.CENTER);
		btnLimit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				boolean selection = btnLimit.getSelection();
				invoiceListView.getInvoiceListContentProvider()
					.setQueryLimit((selection) ? 1000 : -1);
				invoiceListView.refresh();
			}
		});
		btnLimit.setSelection(true);
		btnLimit.setToolTipText(Messages.InvoiceListHeaderComposite_btnLimit_toolTipText);
		btnLimit.setImage(Images.IMG_COUNTER_STOP.getImage());
	}
	
	@Override
	protected void checkSubclass(){
		// Disable the check that prevents subclassing of SWT components
	}
	
	private String[] getBillingSystems(){
		String[] abrechnungsSysteme = BillingSystem.getAbrechnungsSysteme();
		if (abrechnungsSysteme != null) {
			abrechnungsSysteme = ch.elexis.core.ui.preferences.UserCasePreferences
				.sortBillingSystems(abrechnungsSysteme);
		}
		return abrechnungsSysteme;
	}
	
	private void clearValues(){
		comboViewerStatus.setSelection(new StructuredSelection(InvoiceState.OPEN));
		comboViewerType.setSelection(new StructuredSelection(ALL_ELEMENTS_LABEL));
		txtInvoiceno.setText("");
		txtAmount.setText("");
		actPatient = null;
		lblPatientname.setText(ALL_PATIENTS_LABEL);
	}
	
	/**
	 * 
	 * @return the int value of the selected {@link InvoiceState} or <code>null</code> if no valid
	 *         selection
	 */
	Integer getSelectedInvoiceStateNo(){
		StructuredSelection ss = (StructuredSelection) comboViewerStatus.getSelection();
		if (!ss.isEmpty()) {
			Object firstElement = ss.getFirstElement();
			if (firstElement instanceof InvoiceState) {
				return ((InvoiceState) firstElement).numericValue();
			}
		}
		return null;
	}
	
	String getSelectedInvoiceType(){
		StructuredSelection ss = (StructuredSelection) comboViewerType.getSelection();
		if (!ss.isEmpty()) {
			Object firstElement = ss.getFirstElement();
			if (firstElement != ALL_ELEMENTS_LABEL) {
				return (String) firstElement;
			}
		}
		return null;
	}
	
	String getSelectedBillingSystem() {
		StructuredSelection ss = (StructuredSelection) comboViewerBillingSystem.getSelection();
		if(!ss.isEmpty()) {
			Object firstElement = ss.getFirstElement();
			if (firstElement != ALL_ELEMENTS_LABEL) {
				return (String) firstElement;
			}
		}
		return null;
	}
	
	String getSelectedPatientId(){
		if (actPatient != null) {
			return actPatient.getId();
		}
		return null;
	}
	
	String getSelectedInvoiceId(){
		return txtInvoiceno.getText();
	}
	
	String getSelectedTotalAmount(){
		return txtAmount.getText();
	}
	
	/**
	 * Set a Warning if the number of potential results is higher than the query limit set.
	 * 
	 * @param b
	 */
	public void setLimitWarning(Integer queryLimit){
		if (queryLimit != null) {
			float val = queryLimit / 1000f;
			lblLimitWarn.setText(val + "k");
			lblLimitWarn.setToolTipText(
				"Result set was limited to " + queryLimit + ", you do not see all results!");
			lblLimitWarn.setBackground(UiDesk.getColor(UiDesk.COL_RED));
		} else {
			lblLimitWarn.setText("");
			lblLimitWarn.setToolTipText("");
			lblLimitWarn.setBackground(defaultBackgroundColor);
		}
	}
	
}
